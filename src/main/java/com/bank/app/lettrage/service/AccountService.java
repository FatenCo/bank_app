package com.bank.app.lettrage.service;

import com.bank.app.lettrage.entity.*;
import com.bank.app.lettrage.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AccountService {
    public static final double ALERT_THRESHOLD = 0.10;
    private final AccountRepository repo;
    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<Account> findAll() {
        return repo.findAll();
    }

    /** Import manuel “tout ou rien” */
    @Transactional
    public ImportResult importManual(Account acct) {
        List<LogEntry> logs = new ArrayList<>();
        boolean valid = true;

        // Champs obligatoires
        String acctNo      = Optional.ofNullable(acct.getAccountNo()).orElse("").trim();
        String currency    = Optional.ofNullable(acct.getCurrency()).orElse("").trim();
        String customerId  = Optional.ofNullable(acct.getCustomerId()).orElse("").trim();
        String fileName    = Optional.ofNullable(acct.getImportFileName()).orElse("").trim();
        LocalDate date     = acct.getImportDate();

        if (acctNo.isEmpty())   { logs.add(new LogEntry(1, LogEntry.Level.ERROR, "accountNo vide")); valid = false; }
        if (currency.isEmpty()) { logs.add(new LogEntry(1, LogEntry.Level.ERROR, "currency vide"));   valid = false; }
        if (customerId.isEmpty()){ logs.add(new LogEntry(1, LogEntry.Level.ERROR, "customerId vide")); valid = false; }
        if (fileName.isEmpty() || date==null) {
            logs.add(new LogEntry(1, LogEntry.Level.ERROR, "importFileName ou importDate manquant"));
            valid = false;
        }

        // Même fichier déjà importé ?
        if (valid && repo.existsByImportFileNameAndImportDate(fileName, date)) {
            logs.add(new LogEntry(1, LogEntry.Level.ALERT, "⚠️ Ce fichier a déjà été importé"));
            valid = false;
        }
        // AccountNo déjà existant ?
        if (valid && repo.existsByAccountNo(acctNo)) {
            logs.add(new LogEntry(1, LogEntry.Level.ERROR, "accountNo déjà en base : " + acctNo));
            valid = false;
        }

        int total = 1, success = 0;
        if (valid) {
            repo.save(acct);
            logs.add(new LogEntry(1, LogEntry.Level.INFO, "Importée avec succès"));
            success = 1;
        }
        return new ImportResult(total, success, logs, ALERT_THRESHOLD);
    }




    @Transactional
    public ImportResult importFromFile(MultipartFile file) throws Exception {
        String fname = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new IllegalArgumentException("Fichier sans nom"));
        Matcher m = Pattern.compile(".*_(\\d{8})\\..*").matcher(fname);
        LocalDate fileDate = m.matches()
                ? LocalDate.parse(m.group(1), DATE_FMT)
                : LocalDate.now();

        // alerte si déjà importé
        if (repo.existsByImportFileNameAndImportDate(fname, fileDate)) {
            return new ImportResult(
                    0, 0,
                    List.of(new LogEntry(0, LogEntry.Level.ALERT, "⚠️ Ce fichier a déjà été importé")),
                    ALERT_THRESHOLD
            );
        }

        if (fname.toLowerCase().endsWith(".csv")) {
            return importCsv(file.getInputStream(), fname, fileDate);
        } else {
            return importExcel(file.getInputStream(), fname, fileDate);
        }
    }

    private ImportResult importCsv(InputStream is, String fname, LocalDate date) throws Exception {
        List<LogEntry> logs = new ArrayList<>();
        List<Account> toSave = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int total = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null) throw new IllegalArgumentException("Fichier vide");
            String delim = header.contains(";") ? ";" : header.contains("\t") ? "\t" : ",";
            int expectedCols = header.split(Pattern.quote(delim), -1).length;

            String line; int row = 1;
            while ((line = br.readLine()) != null) {
                row++; if (line.isBlank()) continue;
                total++;
                String[] parts = line.split(Pattern.quote(delim), -1);
                boolean valid = true;

                if (parts.length < expectedCols) {
                    logs.add(new LogEntry(row, LogEntry.Level.ERROR, "Colonnes manquantes"));
                    valid = false;
                }
                String acctNo     = parts[0].trim();
                String currency   = parts[5].trim();
                String customerId = parts[6].trim();

                if (acctNo.isEmpty()) {
                    logs.add(new LogEntry(row, LogEntry.Level.ERROR, "accountNo vide")); valid = false;
                }
                if (currency.isEmpty()) {
                    logs.add(new LogEntry(row, LogEntry.Level.ERROR, "currency vide")); valid = false;
                }
                if (customerId.isEmpty()) {
                    logs.add(new LogEntry(row, LogEntry.Level.ERROR, "customerId vide")); valid = false;
                }
                if (!seen.add(acctNo)) {
                    logs.add(new LogEntry(row, LogEntry.Level.ERROR, "Doublon dans le fichier: " + acctNo));
                    valid = false;
                }
                if (repo.existsByAccountNo(acctNo)) {
                    logs.add(new LogEntry(row, LogEntry.Level.ERROR, "accountNo déjà en base: " + acctNo));
                    valid = false;
                }

                if (valid) {
                    toSave.add(Account.builder()
                            .accountNo(acctNo)
                            .shortName(parts[1].trim())
                            .mnemonic(parts[2].trim())
                            .accountOfficer(parts[3].trim())
                            .product(parts[4].trim())
                            .currency(currency)
                            .customerId(customerId)
                            .maCode(parts[7].trim())
                            .accountType(parts[8].trim())
                            .coCode(parts[9].trim())
                            .importFileName(fname)
                            .importDate(date)
                            .build()
                    );
                }
            }
        }

        int success = toSave.size();
        if (total > 0 && success == total) {
            repo.saveAll(toSave);
            for (int i = 0; i < success; i++) {
                logs.add(new LogEntry(i + 2, LogEntry.Level.INFO, "Importée avec succès"));
            }
        }
        return new ImportResult(total, success, logs, ALERT_THRESHOLD);
    }

    private ImportResult importExcel(InputStream is, String fname, LocalDate date) throws Exception {
        List<LogEntry> logs = new ArrayList<>();
        List<Account> toSave = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Workbook wb = WorkbookFactory.create(is);
        Sheet sh = wb.getSheetAt(0);
        int total = 0;
        for (int i = 1; i <= sh.getLastRowNum(); i++) {
            Row r = sh.getRow(i);
            if (r == null) continue;
            total++;
            boolean valid = true;
            String acctNo     = getCell(r, 0);
            String currency   = getCell(r, 5);
            String customerId = getCell(r, 6);

            if (acctNo.isEmpty()) {
                logs.add(new LogEntry(i + 1, LogEntry.Level.ERROR, "accountNo vide")); valid = false;
            }
            if (currency.isEmpty()) {
                logs.add(new LogEntry(i + 1, LogEntry.Level.ERROR, "currency vide")); valid = false;
            }
            if (customerId.isEmpty()) {
                logs.add(new LogEntry(i + 1, LogEntry.Level.ERROR, "customerId vide")); valid = false;
            }
            if (!seen.add(acctNo)) {
                logs.add(new LogEntry(i + 1, LogEntry.Level.ERROR, "Doublon dans le fichier: " + acctNo)); valid = false;
            }
            if (repo.existsByAccountNo(acctNo)) {
                logs.add(new LogEntry(i + 1, LogEntry.Level.ERROR, "accountNo déjà en base: " + acctNo)); valid = false;
            }
            if (valid) {
                toSave.add(Account.builder()
                        .accountNo(acctNo)
                        .shortName(    getCell(r, 1))
                        .mnemonic(     getCell(r, 2))
                        .accountOfficer(getCell(r, 3))
                        .product(      getCell(r, 4))
                        .currency(     currency)
                        .customerId(   customerId)
                        .maCode(       getCell(r, 7))
                        .accountType(  getCell(r, 8))
                        .coCode(       getCell(r, 9))
                        .importFileName(fname)
                        .importDate(date)
                        .build()
                );
            }
        }
        int success = toSave.size();
        if (total > 0 && success == total) {
            repo.saveAll(toSave);
            for (int i = 0; i < success; i++) {
                logs.add(new LogEntry(i + 2, LogEntry.Level.INFO, "Importée avec succès"));
            }
        }
        return new ImportResult(total, success, logs, ALERT_THRESHOLD);
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
