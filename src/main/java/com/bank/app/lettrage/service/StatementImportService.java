package com.bank.app.lettrage.service;

import com.bank.app.lettrage.configuration.DirectoryConfigProperties;
import com.bank.app.lettrage.entity.ImportJob;
import com.bank.app.lettrage.entity.StatementEntry;
import com.bank.app.lettrage.repository.ImportJobRepository;
import com.bank.app.lettrage.repository.StatementEntryRepository;
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
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class StatementImportService {

    private static final Logger log = LoggerFactory.getLogger(StatementImportService.class);
    private final DirectoryConfigProperties cfg;
    private final ImportJobRepository jobRepo;
    private final StatementEntryRepository entryRepo;

    public StatementImportService(DirectoryConfigProperties cfg,
                                  ImportJobRepository jobRepo,
                                  StatementEntryRepository entryRepo) {
        this.cfg = cfg;
        this.jobRepo = jobRepo;
        this.entryRepo = entryRepo;
    }

    // Méthode unique pour le traitement par lots
    public void processStatementsInBatches() {
        log.info("Début du traitement par lots des relevés");
        importByDirectory();
        log.info("Fin du traitement par lots des relevés");
    }

    public ImportJob importManual(List<StatementEntry> entries) {
        ImportJob job = jobRepo.createNew("manual-stmt");
        entries.forEach(e -> e.setImportJob(job));
        entryRepo.saveAll(entries);
        job.complete(entries.size());
        return jobRepo.save(job);
    }

    public CompletableFuture<ImportJob> importByFile(MultipartFile file) {
        String filename = Optional.ofNullable(file.getOriginalFilename())
                .orElse(UUID.randomUUID().toString());
        if (jobRepo.existsBySourceAndStatus(filename, "COMPLETED")) {
            log.warn("Fichier '{}' déjà importé, statut DUPLICATE.", filename);
            ImportJob dup = jobRepo.createNew(filename);
            dup.setStatus("DUPLICATE");
            return CompletableFuture.completedFuture(jobRepo.save(dup));
        }
        ImportJob job = jobRepo.createNew(filename);
        try (InputStream in = file.getInputStream()) {
            List<StatementEntry> list = filename.toLowerCase().endsWith("xls")
                    || filename.toLowerCase().endsWith("xlsx")
                    ? parseExcel(in)
                    : parseCsv(in);
            list.forEach(e -> e.setImportJob(job));
            entryRepo.saveAll(list);
            job.complete(list.size());
            log.info("Import réussi: {} lignes depuis {}", list.size(), filename);
        } catch (Exception ex) {
            log.error("Échec import fichier {}: {}", filename, ex.getMessage());
            job.fail(ex.getMessage());
        }
        return CompletableFuture.completedFuture(jobRepo.save(job));
    }

    public void importByDirectory() {
        Path src = Paths.get(cfg.getStmtsDir());
        Path archive = Paths.get(cfg.getArchiveStmtsDir());
        log.info("Scanning statements dir: {}", src);
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(src)) {
            for (Path p : ds) {
                if (!Files.isRegularFile(p)) continue;
                File f = p.toFile();
                boolean imported = false;
                for (int i = 0; i < 3 && !imported; i++) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        MultipartFile mf = new MockMultipartFile(
                                f.getName(), f.getName(),
                                "application/octet-stream", fis);
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
                        log.warn("Archivage échoué (essai {}) pour {}", i + 1, f.getName());
                        retrySleep(200);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur scanning {}: {}", src, e.getMessage());
        }
    }

    private void retrySleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public Optional<ImportJob> fetchJob(UUID id) {
        return jobRepo.findById(id);
    }

    public List<ImportJob> searchByFileName(String name) {
        return jobRepo.findBySourceContainingIgnoreCase(name);
    }

    public List<ImportJob> searchByDate(String dateStr) {
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            LocalDate date = LocalDate.parse(dateStr);
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.atTime(LocalTime.MAX);
            return jobRepo.findByStartedAtBetween(from, to);
        } else {
            YearMonth m = YearMonth.parse(dateStr);
            return jobRepo.findByStartedAtBetween(
                    m.atDay(1).atStartOfDay(), m.atEndOfMonth().atTime(23, 59, 59)
            );
        }
    }

    public List<StatementEntry> listAll() {
        return entryRepo.findAll();
    }

    public Optional<StatementEntry> getOne(UUID id) {
        return entryRepo.findById(id);
    }

    public Optional<StatementEntry> update(UUID id, StatementEntry u) {
        return entryRepo.findById(id).map(e -> {
            e.setContract(u.getContract());
            e.setCategory(u.getCategory());
            e.setConsolKey(u.getConsolKey());
            e.setCurrency(u.getCurrency());
            e.setCustomerNo(u.getCustomerNo());
            e.setDepartment(u.getDepartment());
            e.setAmtFcy(u.getAmtFcy());
            e.setAmtLcy(u.getAmtLcy());
            e.setResidence(u.getResidence());
            e.setAccountNumber(u.getAccountNumber());
            e.setLclBalConv(u.getLclBalConv());
            e.setAcctDate(u.getAcctDate());
            e.setLocContractType(u.getLocContractType());
            e.setDeptLevel(u.getDeptLevel());
            return entryRepo.save(e);
        });
    }

    public boolean delete(UUID id) {
        return entryRepo.findById(id)
                .map(e -> {
                    entryRepo.delete(e);
                    return true;
                })
                .orElse(false);
    }

    // CSV Parsing
    private List<StatementEntry> parseCsv(InputStream in) throws IOException {
        try (BOMInputStream bom = new BOMInputStream(in,
                ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE);
             Reader r = new InputStreamReader(bom, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withDelimiter(';').parse(r)) {

            Map<String, Integer> hdr = parser.getHeaderMap().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().trim().toUpperCase(), Map.Entry::getValue));
            Function<String[], Integer> idx = keys -> {
                for (String k : keys) if (hdr.containsKey(k)) return hdr.get(k);
                return null;
            };
            List<StatementEntry> out = new ArrayList<>();
            for (CSVRecord rec : parser) {
                StatementEntry e = new StatementEntry();
                e.setContract(rec.get(idx.apply(new String[]{"CONTRACT"})));
                e.setCategory(rec.get(idx.apply(new String[]{"CATEGORY"})));
                e.setConsolKey(rec.get(idx.apply(new String[]{"CONSOL_KEY"})));
                e.setCurrency(rec.get(idx.apply(new String[]{"CURRENCY"})));
                e.setCustomerNo(rec.get(idx.apply(new String[]{"CUSTOMER_NO"})));
                e.setDepartment(rec.get(idx.apply(new String[]{"DEPARTMENT"})));
                e.setAmtFcy(cleanAndValidateDecimal(rec.get(idx.apply(new String[]{"AMT_FCY"}))));
                e.setAmtLcy(cleanAndValidateDecimal(rec.get(idx.apply(new String[]{"AMT_LCY"}))));
                e.setResidence(rec.get(idx.apply(new String[]{"RESIDENCE"})));
                e.setAccountNumber(rec.get(idx.apply(new String[]{"ACCOUNT NUMBER"})));
                e.setLclBalConv(cleanAndValidateDecimal(rec.get(idx.apply(new String[]{"LCL_BAL_CONV"}))));
                e.setAcctDate(rec.get(idx.apply(new String[]{"ACCT_DATE"})));
                e.setLocContractType(rec.get(idx.apply(new String[]{"LOC_CONTRACT_TYPE"})));
                e.setDeptLevel(cleanAndValidateDecimal(rec.get(idx.apply(new String[]{"DEPT_LEVEL"}))));
                out.add(e);
            }
            return out;
        }
    }

    // Excel Parsing
    private List<StatementEntry> parseExcel(InputStream in) throws IOException {
        List<StatementEntry> out = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) return out;
            Map<String, Integer> idx = new HashMap<>();
            for (Cell c : header) idx.put(c.getStringCellValue().trim().toUpperCase(), c.getColumnIndex());
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                StatementEntry e = new StatementEntry();
                e.setContract(getCell(r, idx.get("CONTRACT")));
                e.setCategory(getCell(r, idx.get("CATEGORY")));
                e.setConsolKey(getCell(r, idx.get("CONSOL_KEY")));
                e.setCurrency(getCell(r, idx.get("CURRENCY")));
                e.setCustomerNo(getCell(r, idx.get("CUSTOMER_NO")));
                e.setDepartment(getCell(r, idx.get("DEPARTMENT")));
                e.setAmtFcy(parseCellNum(r, idx.get("AMT_FCY")));
                e.setAmtLcy(parseCellNum(r, idx.get("AMT_LCY")));
                e.setResidence(getCell(r, idx.get("RESIDENCE")));
                e.setAccountNumber(getCell(r, idx.get("ACCOUNT NUMBER")));
                e.setLclBalConv(parseCellNum(r, idx.get("LCL_BAL_CONV")));
                e.setAcctDate(getCell(r, idx.get("ACCT_DATE")));
                e.setLocContractType(getCell(r, idx.get("LOC_CONTRACT_TYPE")));
                e.setDeptLevel(parseCellNum(r, idx.get("DEPT_LEVEL")));
                out.add(e);
            }
        }
        return out;
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

    // Clean up and parse decimal values
    private BigDecimal cleanAndValidateDecimal(String s) {
        try {
            return new BigDecimal(s.trim().replace(" ", "").replace(',', '.'));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}