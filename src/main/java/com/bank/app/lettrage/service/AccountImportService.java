package com.bank.app.lettrage.service;

import com.bank.app.lettrage.configuration.DirectoryConfigProperties;
import com.bank.app.lettrage.entity.AccountEntry;
import com.bank.app.lettrage.entity.ImportJob;
import com.bank.app.lettrage.repository.AccountRepository;
import com.bank.app.lettrage.repository.ImportJobRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.poi.ss.usermodel.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

    // Méthode unique pour le traitement par lots
    public void processAccountsInBatches() {
        log.info("Début du traitement par lots des comptes");
        importByDirectory();
        log.info("Fin du traitement par lots des comptes");
    }

    /**
     * Import manuel via JSON payload
     */
    public ImportJob importManual(List<AccountEntry> entries) {
        ImportJob job = jobRepo.createNew("manual-import");
        entries.forEach(e -> e.setImportJob(job));
        accountRepo.saveAll(entries);
        job.complete(entries.size());
        return jobRepo.save(job);
    }

    /**
     * Import via upload d'un fichier CSV ou Excel
     */
    public CompletableFuture<ImportJob> importByFile(MultipartFile file) {
        String filename = Optional.ofNullable(file.getOriginalFilename())
                .orElse(UUID.randomUUID().toString());
        if (jobRepo.existsBySourceAndStatus(filename, "COMPLETED")) {
            log.warn("Fichier déjà importé : {}", filename);
            ImportJob dup = jobRepo.createNew(filename);
            dup.setStatus("DUPLICATE");
            return CompletableFuture.completedFuture(jobRepo.save(dup));
        }
        ImportJob job = jobRepo.createNew(filename);
        try (InputStream in = file.getInputStream()) {
            List<AccountEntry> entries = filename.toLowerCase().endsWith("xls")
                    || filename.toLowerCase().endsWith("xlsx")
                    ? parseExcel(in)
                    : parseCsv(in);
            entries.forEach(e -> e.setImportJob(job));
            accountRepo.saveAll(entries);
            job.complete(entries.size());
            log.info("[IMPORT] {} lignes importées depuis '{}'", entries.size(), filename);
        } catch (Exception ex) {
            log.error("[IMPORT] Échec de l'import : {}", filename, ex);
            job.fail(ex.getMessage());
        }
        return CompletableFuture.completedFuture(jobRepo.save(job));
    }

    /**
     * Import synchrones depuis le dossier source et archivage
     */
    public void importByDirectory() {
        Path src     = Paths.get(cfg.getAccountsDir());
        Path archive = Paths.get(cfg.getArchiveAccountsDir());
        log.info("Scanning accounts dir: {}", src);
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(src)) {
            for (Path p : ds) {
                if (!Files.isRegularFile(p)) continue;
                File f = p.toFile();
                boolean imported = false;
                for (int i = 0; i < 3 && !imported; i++) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        MultipartFile mf = new MockMultipartFile(
                                f.getName(), f.getName(), "application/octet-stream", fis);
                        importByFile(mf).join();
                        imported = true;
                    } catch (FileNotFoundException fnf) {
                        log.warn("Fichier verrouillé, retry import {}", f.getName());
                        retrySleep(200);
                    } catch (Exception ex) {
                        log.error("Erreur import {}: {}", f.getName(), ex.getMessage());
                        break;
                    }
                }
                if (!imported) continue;
                for (int i = 0; i < 3; i++) {
                    try {
                        if (!Files.exists(archive)) Files.createDirectories(archive);
                        Files.move(p, archive.resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING);
                        log.info("Archivé {} -> {}", f.getName(), archive);
                        break;
                    } catch (IOException ioe) {
                        log.warn("Archivage échoué (essai {}) pour {}", i+1, f.getName());
                        retrySleep(200);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur scanning {}: {}", src, e.getMessage());
        }
    }

    /**
     * Petite pause entre retries
     */
    private void retrySleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Récupère un ImportJob par ID
     */
    public Optional<ImportJob> fetchJob(UUID id) {
        return jobRepo.findById(id);
    }

    /**
     * Recherche des jobs par nom de fichier
     */
    public List<ImportJob> searchJobsByFileName(String namePart) {
        return jobRepo.findBySourceContainingIgnoreCase(namePart);
    }

    /**
     * Recherche des jobs par date (YYYY-MM-DD)
     */
    public List<ImportJob> searchJobsByDate(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to   = date.atTime(LocalTime.MAX);
        return jobRepo.findByStartedAtBetween(from, to);
    }

    /**
     * Liste tous les AccountEntry
     */
    public List<AccountEntry> listEntries() {
        return accountRepo.findAll();
    }

    /**
     * Récupère un AccountEntry par ID
     */
    public Optional<AccountEntry> getEntry(UUID id) {
        return accountRepo.findById(id);
    }

    /**
     * Met à jour un AccountEntry existant
     */
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

    /**
     * Supprime un AccountEntry
     */
    public boolean deleteEntry(UUID id) {
        return accountRepo.findById(id)
                .map(e -> { accountRepo.delete(e); return true; })
                .orElse(false);
    }

    /**
     * Parse un CSV avec BOM et header insensible à la casse
     */
    private List<AccountEntry> parseCsv(InputStream in) throws IOException {
        try (BOMInputStream bom = new BOMInputStream(in,
                ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE);
             Reader r = new InputStreamReader(bom, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withDelimiter(';')
                     .withTrim()
                     .parse(r)) {
            Map<String,Integer> headers = parser.getHeaderMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().trim().toLowerCase(),
                            Map.Entry::getValue
                    ));
            Function<String[],Integer> colIdx = keys -> {
                for (String k: keys) {
                    Integer idx = headers.get(k.toLowerCase());
                    if (idx != null) return idx;
                }
                return null;
            };
            Integer iDate    = colIdx.apply(new String[]{"date"});
            Integer iTxn     = colIdx.apply(new String[]{"transaction"});
            Integer iAmt     = colIdx.apply(new String[]{"amount","montant"});
            Integer iEnt     = colIdx.apply(new String[]{"entity","entité"});
            Integer iRem     = colIdx.apply(new String[]{"remarks","observations"});
            Integer iAcct    = colIdx.apply(new String[]{"account number"});
            Integer iTot     = colIdx.apply(new String[]{"total"});
            List<AccountEntry> list = new ArrayList<>();
            for (CSVRecord rec: parser) {
                AccountEntry e = new AccountEntry();
                e.setDateOperation(rec.get(iDate));
                e.setTransactionId(rec.get(iTxn));
                e.setAmount(cleanAndValidateDecimal(rec.get(iAmt)));
                e.setEntity(rec.get(iEnt));
                e.setRemarks(rec.get(iRem));
                e.setAccountNumber(rec.get(iAcct));
                String tot = iTot != null ? rec.get(iTot) : "";
                e.setTotal(tot.isBlank() ? BigDecimal.ZERO : cleanAndValidateDecimal(tot));
                list.add(e);
            }
            return list;
        }
    }

    /**
     * Parse un Excel (XLS/XLSX) générique
     */
    private List<AccountEntry> parseExcel(InputStream in) throws IOException {
        List<AccountEntry> list = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            Row hdr = sheet.getRow(0);
            if (hdr == null) return list;
            Map<String, Integer> idx = new HashMap<>();
            for (Cell c : hdr) idx.put(c.getStringCellValue().trim().toLowerCase(), c.getColumnIndex());
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
                list.add(e);
            }
        }
        return list;
    }

    private String getCell(Row r, Integer idx) {
        if (idx == null) return "";
        Cell c = r.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return c != null ? c.toString().trim() : "";
    }

    private BigDecimal parseCellNum(Row r, Integer idx) {
        String s = getCell(r, idx);
        return s.isBlank() ? BigDecimal.ZERO : cleanAndValidateDecimal(s);
    }

    private BigDecimal cleanAndValidateDecimal(String s) {
        try {
            return new BigDecimal(s.trim().replace(" ", "").replace(',', '.'));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}