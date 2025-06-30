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
import java.nio.file.*;
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

    // IMPORT MANUEL
    public ImportJob importManual(List<StatementEntry> entries) {
        ImportJob job = jobRepo.createNew("manual-stmt");
        entries.forEach(e -> e.setImportJob(job));
        entryRepo.saveAll(entries);
        job.complete(entries.size());
        return jobRepo.save(job);
    }

    // IMPORT PAR FICHIER CSV / EXCEL
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
            List<StatementEntry> entries = filename.toLowerCase().endsWith("xls")
                    || filename.toLowerCase().endsWith("xlsx")
                    ? parseExcel(in)
                    : parseCsv(in);
            entries.forEach(e -> e.setImportJob(job));
            entryRepo.saveAll(entries);
            job.complete(entries.size());
            log.info("Import réussi: {} lignes depuis {}", entries.size(), filename);
        } catch (Exception e) {
            log.error("Échec import fichier {}: {}", filename, e.getMessage());
            job.fail(e.getMessage());
        }
        return CompletableFuture.completedFuture(jobRepo.save(job));
    }

    // IMPORT PAR DOSSIER & ARCHIVAGE AVEC RETRIES
    @Async
    public void importByDirectory() {
        Path src     = Paths.get(cfg.getStmtsDir());
        Path archive = Paths.get(cfg.getArchiveStmtsDir());
        log.info("Scanning statements dir: {}", src);
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(src)) {
            for (Path p : ds) {
                if (!Files.isRegularFile(p)) continue;
                File f = p.toFile();
                // IMPORT AVEC RETRY SI LOCKED
                int attempts = 3;
                boolean imported = false;
                while (attempts-- > 0 && !imported) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        MultipartFile mf = new MockMultipartFile(
                                f.getName(), f.getName(), "application/octet-stream", fis);
                        importByFile(mf).join();
                        imported = true;
                    } catch (FileNotFoundException fnf) {
                        log.warn("Fichier verrouillé, retry import {} in 200ms", f.getName());
                        sleep(200);
                    } catch (Exception ex) {
                        log.error("Erreur import {}: {}", f.getName(), ex.getMessage());
                        break;
                    }
                }
                if (!imported) {
                    log.error("Import échoué après retries pour {}", f.getName());
                    continue;
                }
                // ARCHIVAGE AVEC RETRY
                attempts = 3;
                while (attempts-- > 0) {
                    try {
                        if (!Files.exists(archive)) Files.createDirectories(archive);
                        Files.move(p, archive.resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING);
                        log.info("Archivé {} -> {}", f.getName(), archive);
                        break;
                    } catch (IOException ioe) {
                        log.warn("Archivage échoué, retry {} for {}", (3 - attempts), f.getName());
                        sleep(200);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Erreur scanning {}: {}", src, e.getMessage());
        }
    }

    private void sleep(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    // CRUD IMPORT JOBS
    public Optional<ImportJob> fetchJob(UUID id) { return jobRepo.findById(id); }
    public List<ImportJob> searchByFileName(String name) { return jobRepo.findBySourceContainingIgnoreCase(name); }
    public List<ImportJob> searchByDate(String ym) {
        YearMonth m = YearMonth.parse(ym);
        return jobRepo.findByStartedAtBetween(
                m.atDay(1).atStartOfDay(), m.atEndOfMonth().atTime(23,59,59));
    }

    // CRUD STATEMENT ENTRIES
    public List<StatementEntry> listAll() { return entryRepo.findAll(); }
    public Optional<StatementEntry> getOne(UUID id) { return entryRepo.findById(id); }
    public Optional<StatementEntry> update(UUID id, StatementEntry u) {
        return entryRepo.findById(id).map(e -> {
            e.setContract(u.getContract()); e.setCategory(u.getCategory());
            e.setConsolKey(u.getConsolKey()); e.setCurrency(u.getCurrency());
            e.setCustomerNo(u.getCustomerNo()); e.setDepartment(u.getDepartment());
            e.setAmtFcy(u.getAmtFcy()); e.setAmtLcy(u.getAmtLcy());
            e.setResidence(u.getResidence()); e.setAccountNumber(u.getAccountNumber());
            e.setLclBalConv(u.getLclBalConv()); e.setAcctDate(u.getAcctDate());
            e.setLocContractType(u.getLocContractType()); e.setDeptLevel(u.getDeptLevel());
            return entryRepo.save(e);
        });
    }
    public boolean delete(UUID id) {
        return entryRepo.findById(id).map(e -> { entryRepo.delete(e); return true; }).orElse(false);
    }

    // PARSING CSV
    private List<StatementEntry> parseCsv(InputStream in) throws IOException {
        BOMInputStream bom = new BOMInputStream(in,
                ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE);
        try (Reader r = new InputStreamReader(bom, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .withDelimiter(';')
                     .parse(r)) {
            Map<String,Integer> hdr = parser.getHeaderMap().entrySet().stream()
                    .collect(Collectors.toMap(e->e.getKey().trim().toUpperCase(), Map.Entry::getValue));
            Function<String[],Integer> idx = keys -> {
                for (String k: keys) if (hdr.containsKey(k)) return hdr.get(k);
                return null;
            };
            List<StatementEntry> out = new ArrayList<>();
            for (CSVRecord rec: parser) {
                StatementEntry e = new StatementEntry();
                e.setContract(rec.get(idx.apply(new String[]{"CONTRACT"})));
                e.setCategory(rec.get(idx.apply(new String[]{"CATEGORY"})));
                e.setConsolKey(rec.get(idx.apply(new String[]{"CONSOL_KEY"})));
                e.setCurrency(rec.get(idx.apply(new String[]{"CURRENCY"})));
                e.setCustomerNo(rec.get(idx.apply(new String[]{"CUSTOMER_NO"})));
                e.setDepartment(rec.get(idx.apply(new String[]{"DEPARTMENT"})));
                e.setAmtFcy(parseDecimal(rec.get(idx.apply(new String[]{"AMT_FCY"}))));
                e.setAmtLcy(parseDecimal(rec.get(idx.apply(new String[]{"AMT_LCY"}))));
                e.setResidence(rec.get(idx.apply(new String[]{"RESIDENCE"})));
                e.setAccountNumber(rec.get(idx.apply(new String[]{"ACCOUNT NUMBER"})));
                e.setLclBalConv(parseDecimal(rec.get(idx.apply(new String[]{"LCL_BAL_CONV"}))));
                e.setAcctDate(rec.get(idx.apply(new String[]{"ACCT_DATE"})));
                e.setLocContractType(rec.get(idx.apply(new String[]{"LOC_CONTRACT_TYPE"})));
                e.setDeptLevel(parseDecimal(rec.get(idx.apply(new String[]{"DEPT_LEVEL"}))));
                out.add(e);
            }
            return out;
        }
    }

    // PARSING EXCEL
    private List<StatementEntry> parseExcel(InputStream in) throws IOException {
        List<StatementEntry> out = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) return out;
            Map<String,Integer> idx = new HashMap<>();
            for (Cell c: header) idx.put(c.getStringCellValue().trim().toUpperCase(), c.getColumnIndex());
            for (int i=1; i<=sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i); if (r==null) continue;
                StatementEntry e = new StatementEntry();
                e.setContract(getCell(r,idx.get("CONTRACT")));
                e.setCategory(getCell(r,idx.get("CATEGORY")));
                e.setConsolKey(getCell(r,idx.get("CONSOL_KEY")));
                e.setCurrency(getCell(r,idx.get("CURRENCY")));
                e.setCustomerNo(getCell(r,idx.get("CUSTOMER_NO")));
                e.setDepartment(getCell(r,idx.get("DEPARTMENT")));
                e.setAmtFcy(parseCellNum(r,idx.get("AMT_FCY")));
                e.setAmtLcy(parseCellNum(r,idx.get("AMT_LCY")));
                e.setResidence(getCell(r,idx.get("RESIDENCE")));
                e.setAccountNumber(getCell(r,idx.get("ACCOUNT NUMBER")));
                e.setLclBalConv(parseCellNum(r,idx.get("LCL_BAL_CONV")));
                e.setAcctDate(getCell(r,idx.get("ACCT_DATE")));
                e.setLocContractType(getCell(r,idx.get("LOC_CONTRACT_TYPE")));
                e.setDeptLevel(parseCellNum(r,idx.get("DEPT_LEVEL")));
                out.add(e);
            }
        }
        return out;
    }

    private String getCell(Row r,Integer idx) {
        if (idx==null) return "";
        Cell c = r.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return c!=null?c.toString().trim():"";
    }

    private BigDecimal parseCellNum(Row r,Integer idx) { return parseDecimal(getCell(r,idx)); }
    private BigDecimal parseDecimal(String s) {
        if (s==null || s.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(s.trim().replace(" ","").replace(',','.'));
    }
}
