package com.bank.app.lettrage.service;

import com.bank.app.lettrage.entity.Account;
import com.bank.app.lettrage.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository repo;
    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Account saveManual(Account account) {
        return repo.save(account);
    }

    public List<String> importFromFile(MultipartFile file) throws Exception {
        String original = file.getOriginalFilename();
        if (original == null) {
            throw new IllegalArgumentException("Fichier sans nom");
        }

        // Extraction de la date dans le nom (ex : 11025_UQ.ACCOUNT_20250321.csv → 20250321)
        Matcher m = Pattern.compile(".*_(\\d{8})\\..*").matcher(original);
        LocalDate fileDate = (m.matches())
                ? LocalDate.parse(m.group(1), DATE_FMT)
                : LocalDate.now();

        if (original.toLowerCase().endsWith(".csv")) {
            return importFromCsv(file.getInputStream(), original, fileDate);
        } else {
            return importFromExcel(file.getInputStream(), original, fileDate);
        }
    }

    private List<String> importFromCsv(InputStream is, String fname, LocalDate date) throws Exception {
        List<String> logs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Fichier vide");
            }

            // Détection du séparateur
            String delim = headerLine.contains(";") ? ";" :
                    headerLine.contains("\t") ? "\t" : ",";

            // Nombre de colonnes attendu
            int expectedCols = headerLine.split(Pattern.quote(delim), -1).length;

            String line;
            int row = 1; // on commence à 1 pour l’en-tête
            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;

                String[] parts = line.split(Pattern.quote(delim), -1);
                if (parts.length < expectedCols) {
                    logs.add("Ligne " + row + " : colonnes manquantes ("
                            + parts.length + "/" + expectedCols + ")");
                    continue;
                }

                try {
                    Account a = Account.builder()
                            .accountNo(parts[0].trim())
                            .shortName(parts[1].trim())
                            .mnemonic(parts[2].trim())
                            .accountOfficer(parts[3].trim())
                            .product(parts[4].trim())
                            .currency(parts[5].trim())
                            .customerId(parts[6].trim())
                            .maCode(parts[7].trim())
                            .accountType(parts[8].trim())
                            .coCode(parts[9].trim())
                            .importFileName(fname)
                            .importDate(date)
                            .build();
                    repo.save(a);
                    logs.add("Ligne " + row + " importée");
                } catch (Exception ex) {
                    logs.add("Erreur ligne " + row + " : " + ex.getMessage());
                }
            }
        }
        return logs;
    }

    private List<String> importFromExcel(InputStream is, String fname, LocalDate date) throws Exception {
        List<String> logs = new ArrayList<>();
        Workbook wb = WorkbookFactory.create(is);
        Sheet sh = wb.getSheetAt(0);
        for (int i = 1; i <= sh.getLastRowNum(); i++) {
            Row r = sh.getRow(i);
            if (r == null) continue;
            try {
                Account a = Account.builder()
                        .accountNo(getCell(r, 0))
                        .shortName(getCell(r, 1))
                        .mnemonic(getCell(r, 2))
                        .accountOfficer(getCell(r, 3))
                        .product(getCell(r, 4))
                        .currency(getCell(r, 5))
                        .customerId(getCell(r, 6))
                        .maCode(getCell(r, 7))
                        .accountType(getCell(r, 8))
                        .coCode(getCell(r, 9))
                        .importFileName(fname)
                        .importDate(date)
                        .build();
                repo.save(a);
                logs.add("Ligne " + (i + 1) + " importée");
            } catch (Exception e) {
                logs.add("Erreur ligne " + (i + 1) + " : " + e.getMessage());
            }
        }
        return logs;
    }

    private String getCell(Row row, int idx) {
        Cell c = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return switch (c.getCellType()) {
            case STRING  -> c.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(c.getNumericCellValue());
            default       -> "";
        };
    }
}
