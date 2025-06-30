package com.bank.app.lettrage.service;

import com.bank.app.lettrage.configuration.DirectoryConfigProperties;
import com.bank.app.lettrage.entity.AccountEntry;
import com.bank.app.lettrage.entity.ImportJob;
import com.bank.app.lettrage.repository.AccountRepository;
import com.bank.app.lettrage.repository.ImportJobRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountImportService {

    private static final Logger log = LoggerFactory.getLogger(AccountImportService.class);

    private final DirectoryConfigProperties cfg;
    private final ImportJobRepository jobRepo;
    private final AccountRepository accountRepo;

    public AccountImportService(DirectoryConfigProperties cfg,
                                ImportJobRepository jobRepo,
                                AccountRepository accountRepo) {
        this.cfg = cfg;
        this.jobRepo = jobRepo;
        this.accountRepo = accountRepo;
    }

    // ======================
    // IMPORTATION DES DONNÉES
    // ======================

    /** Import manuel d’une liste d’entrées */
    public ImportJob importManual(List<AccountEntry> entries) {
        ImportJob job = jobRepo.createNew("manual-import");
        entries.forEach(e -> e.setImportJob(job));
        accountRepo.saveAll(entries);
        job.complete(entries.size());
        return jobRepo.save(job);
    }

    /** Import asynchrone depuis un fichier (CSV | Excel) */
    public CompletableFuture<ImportJob> importByFile(MultipartFile file) {
        String filename = Optional.ofNullable(file.getOriginalFilename())
                .orElse(UUID.randomUUID().toString());

        // anti‑doublon
        if (jobRepo.existsBySourceAndStatus(filename, "COMPLETED")) {
            log.warn("Fichier déjà importé : {}", filename);
            ImportJob dup = jobRepo.createNew(filename);
            dup.setStatus("DUPLICATE");
            return CompletableFuture.completedFuture(jobRepo.save(dup));
        }

        ImportJob job = jobRepo.createNew(filename);
        try (InputStream in = file.getInputStream()) {
            List<AccountEntry> entries;
            String lower = filename.toLowerCase();
            if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) {
                entries = parseExcel(in);
            } else {
                entries = parseCsv(in);
            }

            entries.forEach(e -> e.setImportJob(job));
            accountRepo.saveAll(entries);

            job.complete(entries.size());
            log.info("[IMPORT] {} lignes importées depuis '{}'", entries.size(), filename);
        } catch (Exception ex) {
            log.error("[IMPORT] Échec de l'import : {}", filename, ex);
            job.fail(ex.getMessage());
        }

        return CompletableFuture.completedFuture(jobRepo.save(job));
    }

    /** Scanner périodiquement le dossier et importer tout nouveau fichier CSV/XLS(X) */
    @Async
    public void importByDirectory() {
        Path dir = Paths.get(cfg.getAccountsDir());
        log.info("Scanning accounts dir: {}", dir);
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            for (Path p : ds) {
                File f = p.toFile();
                if (!f.isFile()) continue;
                try (FileInputStream fis = new FileInputStream(f)) {
                    MultipartFile mf = new MockMultipartFile(
                            f.getName(), f.getName(),
                            "application/octet-stream", fis);
                    importByFile(mf).join();
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors du scanning du dossier {}", dir, e);
        }
    }

    // ======================
    // RECHERCHE & CRUD
    // ======================

    public Optional<ImportJob> fetchJob(UUID id) {
        return jobRepo.findById(id);
    }

    public List<ImportJob> searchJobsByFileName(String namePart) {
        return jobRepo.findBySourceContainingIgnoreCase(namePart);
    }

    public List<ImportJob> searchJobsByDate(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to   = date.atTime(LocalTime.MAX);
        return jobRepo.findByStartedAtBetween(from, to);
    }

    public List<AccountEntry> listEntries() {
        return accountRepo.findAll();
    }

    public Optional<AccountEntry> getEntry(UUID id) {
        return accountRepo.findById(id);
    }

    public Optional<AccountEntry> updateEntry(UUID id, AccountEntry upd) {
        return accountRepo.findById(id).map(existing -> {
            existing.setDateOperation(upd.getDateOperation());
            existing.setTransactionId(upd.getTransactionId());
            existing.setAmount(upd.getAmount());
            existing.setEntity(upd.getEntity());
            existing.setRemarks(upd.getRemarks());
            existing.setAccountNumber(upd.getAccountNumber());
            existing.setTotal(upd.getTotal());
            return accountRepo.save(existing);
        });
    }

    public boolean deleteEntry(UUID id) {
        return accountRepo.findById(id).map(e -> {
            accountRepo.delete(e);
            return true;
        }).orElse(false);
    }

    // ======================
    // PARSERS CSV & EXCEL
    // ======================

    private List<AccountEntry> parseCsv(InputStream in) throws IOException {
        BOMInputStream bom = new BOMInputStream(in,
                ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE);
        try (Reader reader = new InputStreamReader(bom, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withDelimiter(';')
                     .withIgnoreEmptyLines(false)
                     .withTrim()
                     .parse(reader)) {

            // Mapping case-insensitive des headers
            Map<String, Integer> headers = parser.getHeaderMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().trim().toLowerCase(),
                            Map.Entry::getValue
                    ));

            // Fonction pour récupérer l'index d'une colonne (anglais, puis français)
            Function<String[], Integer> colIndex = keys -> {
                for (String k : keys) {
                    Integer idx = headers.get(k.toLowerCase());
                    if (idx != null) return idx;
                }
                return null;
            };

            Integer idxDate    = colIndex.apply(new String[]{"Date"});
            Integer idxTxn     = colIndex.apply(new String[]{"Transaction"});
            Integer idxAmount  = colIndex.apply(new String[]{"Amount", "Montant"});
            Integer idxEntity  = colIndex.apply(new String[]{"Entity", "Entité"});
            Integer idxRemarks = colIndex.apply(new String[]{"Remarks", "Observations"});
            Integer idxAcctNum = colIndex.apply(new String[]{"Account Number"});
            Integer idxTotal   = colIndex.apply(new String[]{"total"});

            List<AccountEntry> out = new ArrayList<>();
            for (CSVRecord rec : parser) {
                AccountEntry e = new AccountEntry();
                e.setDateOperation(rec.get(idxDate));
                e.setTransactionId(rec.get(idxTxn));
                e.setAmount(parseAmount(rec.get(idxAmount)));
                e.setEntity(rec.get(idxEntity));
                e.setRemarks(rec.get(idxRemarks));
                e.setAccountNumber(rec.get(idxAcctNum));
                String tot = (idxTotal != null) ? rec.get(idxTotal) : "";
                e.setTotal(tot.isBlank() ? BigDecimal.ZERO : parseAmount(tot));
                out.add(e);
            }
            return out;
        }
    }

    private List<AccountEntry> parseExcel(InputStream in) throws IOException {
        List<AccountEntry> out = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) return Collections.emptyList();

            Map<String,Integer> idx = new HashMap<>();
            for (Cell c : header) {
                idx.put(c.getStringCellValue().trim().toLowerCase(), c.getColumnIndex());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                AccountEntry e = new AccountEntry();
                e.setDateOperation(getCell(r, idx.get("date")));
                e.setTransactionId(getCell(r, idx.get("transaction")));
                e.setAmount(parseCellNum(r, idx.get("amount")));
                e.setEntity(getCell(r, idx.get("entity")));
                e.setRemarks(getCell(r, idx.get("remarks")));
                e.setAccountNumber(getCell(r, idx.get("account number")));
                e.setTotal(parseCellNum(r, idx.get("total")));
                out.add(e);
            }
        }
        return out;
    }

    // ======================
    // UTILS PARSING
    // ======================

    private BigDecimal parseAmount(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(s.trim().replace(" ", "").replace(',', '.'));
    }

    private BigDecimal parseCellNum(Row r, Integer i) {
        if (i == null) return BigDecimal.ZERO;
        Cell c = r.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (c == null) return BigDecimal.ZERO;
        if (c.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(c.getNumericCellValue());
        }
        return parseAmount(c.toString());
    }

    private String getCell(Row r, Integer i) {
        if (i == null) return "";
        Cell c = r.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return c != null ? c.toString().trim() : "";
    }
}
