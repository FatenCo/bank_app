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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class StatementImportService {

    private static final Logger log = LoggerFactory.getLogger(StatementImportService.class);

    private final DirectoryConfigProperties cfg;
    private final ImportJobRepository jobRepo;
    private final StatementEntryRepository entryRepo;

    public StatementImportService(
            DirectoryConfigProperties cfg,
            ImportJobRepository jobRepo,
            StatementEntryRepository entryRepo
    ) {
        this.cfg = cfg;
        this.jobRepo = jobRepo;
        this.entryRepo = entryRepo;
    }

    /** Import manuel via JSON payload */
    public ImportJob importManual(List<StatementEntry> entries) {
        ImportJob job = jobRepo.createNew("manual-stmt");
        entries.forEach(e -> e.setImportJob(job));
        entryRepo.saveAll(entries);
        job.complete(entries.size());
        return jobRepo.save(job);
    }

    /** Import par fichier CSV ou Excel (async) */
    public CompletableFuture<ImportJob> importByFile(MultipartFile file) {
        String filename = Optional.ofNullable(file.getOriginalFilename())
                .orElse(UUID.randomUUID().toString());

        if (jobRepo.existsBySourceAndStatus(filename, "COMPLETED")) {
            log.warn("Fichier '{}' déjà importé, ignorer.", filename);
            ImportJob dup = jobRepo.createNew(filename);
            dup.setStatus("DUPLICATE");
            return CompletableFuture.completedFuture(jobRepo.save(dup));
        }

        ImportJob job = jobRepo.createNew(filename);
        try (InputStream in = file.getInputStream()) {
            List<StatementEntry> list = filename.toLowerCase().endsWith(".xls")
                    || filename.toLowerCase().endsWith(".xlsx")
                    ? parseExcel(in)
                    : parseCsv(in);

            list.forEach(e -> e.setImportJob(job));
            entryRepo.saveAll(list);

            job.complete(list.size());
            log.info("Import réussi de {} lignes depuis '{}'", list.size(), filename);
        } catch (Exception ex) {
            log.error("Échec import du fichier '{}', {}", filename, ex.getMessage());
            job.fail(ex.getMessage());
        }
        return CompletableFuture.completedFuture(jobRepo.save(job));
    }

    /** Import automatique périodique depuis le dossier configuré */
    @Async
    public void importByDirectory() {
        Path dir = Paths.get(cfg.getStmtsDir());
        log.info("Scanning statements dir: {}", dir);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
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
            log.error("Erreur de scan du dossier statements", e);
        }
    }

    // === Recherche et CRUD des jobs ===

    public Optional<ImportJob> fetchJob(UUID id) {
        return jobRepo.findById(id);
    }

    public List<ImportJob> searchByFileName(String name) {
        return jobRepo.findBySourceContainingIgnoreCase(name);
    }

    public List<ImportJob> searchByDate(String ym) {
        YearMonth m = YearMonth.parse(ym);
        return jobRepo.findByStartedAtBetween(
                m.atDay(1).atStartOfDay(),
                m.atEndOfMonth().atTime(23, 59, 59)
        );
    }

    // === CRUD des entrées StatementEntry ===

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
                .map(e -> { entryRepo.delete(e); return true; })
                .orElse(false);
    }

    // === Parsers CSV & Excel ===

    private List<StatementEntry> parseCsv(InputStream in) throws IOException {
        BOMInputStream bom = new BOMInputStream(in,
                ByteOrderMark.UTF_8,
                ByteOrderMark.UTF_16LE,
                ByteOrderMark.UTF_16BE);
        try (Reader r = new InputStreamReader(bom, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .withDelimiter(';')
                     .parse(r)) {

            List<StatementEntry> out = new ArrayList<>();
            for (CSVRecord rec : parser) {
                StatementEntry e = new StatementEntry();
                e.setContract(rec.get("CONTRACT"));
                e.setCategory(rec.get("CATEGORY"));
                e.setConsolKey(rec.get("CONSOL_KEY"));
                e.setCurrency(rec.get("CURRENCY"));
                e.setCustomerNo(rec.get("CUSTOMER_NO"));
                e.setDepartment(rec.get("DEPARTMENT"));
                e.setAmtFcy(parseDecimal(rec.get("AMT_FCY")));
                e.setAmtLcy(parseDecimal(rec.get("AMT_LCY")));
                e.setResidence(rec.get("RESIDENCE"));
                e.setAccountNumber(rec.get("Account Number"));
                e.setLclBalConv(parseDecimal(rec.get("LCL_BAL_CONV")));
                e.setAcctDate(rec.get("ACCT_DATE"));
                e.setLocContractType(rec.get("LOC_CONTRACT_TYPE"));
                e.setDeptLevel(parseDecimal(rec.get("DEPT_LEVEL")));
                out.add(e);
            }
            return out;
        }
    }

    private List<StatementEntry> parseExcel(InputStream in) throws IOException {
        List<StatementEntry> out = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) return out;
            var idx = new java.util.HashMap<String,Integer>();
            for (Cell c : header) {
                idx.put(c.getStringCellValue().trim().toUpperCase(), c.getColumnIndex());
            }

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

    // === Helpers Parsing ===

    private String getCell(Row r, Integer idx) {
        if (idx == null) return "";
        Cell c = r.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return c != null ? c.toString().trim() : "";
    }

    private BigDecimal parseCellNum(Row r, Integer idx) {
        String s = getCell(r, idx);
        return parseDecimal(s);
    }

    private BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        String norm = s.trim().replace(" ", "").replace(',', '.');
        return new BigDecimal(norm);
    }
}
