package com.bank.app.lettrage.service;

import com.bank.app.lettrage.configuration.DirectoryConfigProperties;
import com.bank.app.lettrage.repository.ImportJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;

@Service
public class DirectoryWatcherService {

    private static final Logger log = LoggerFactory.getLogger(DirectoryWatcherService.class);
    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final DirectoryConfigProperties cfg;
    private final StatementImportService stmtService;
    private final AccountImportService accountService;
    private final ImportJobRepository jobRepo;

    public DirectoryWatcherService(
            DirectoryConfigProperties cfg,
            StatementImportService stmtService,
            AccountImportService accountService,
            ImportJobRepository jobRepo
    ) {
        this.cfg = cfg;
        this.stmtService = stmtService;
        this.accountService = accountService;
        this.jobRepo = jobRepo;
    }

    @PostConstruct
    public void start() {
        Executors.newSingleThreadExecutor()
                .submit(() -> watchDirectory(cfg.getAccountsDir(), "accounts"));
        Executors.newSingleThreadExecutor()
                .submit(() -> watchDirectory(cfg.getStmtsDir(),    "statements"));
    }

    private void watchDirectory(String dirPath, String type) {
        try {
            WatchService ws = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(dirPath);
            dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
            log.info("[Watcher] Start watching {}", dirPath);

            while (true) {
                WatchKey key = ws.take();
                for (WatchEvent<?> ev : key.pollEvents()) {
                    if (ev.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        File file = dir.resolve((Path)ev.context()).toFile();
                        handleFile(file, type);
                    }
                }
                key.reset();
            }
        } catch (Exception e) {
            log.error("[Watcher] Error watching {}: ", dirPath, e);
        }
    }

    private void handleFile(File file, String type) {
        String name = file.getName();
        // anti‐doublon
        if (jobRepo.existsBySourceAndStatus(name, "COMPLETED")) {
            log.warn("[Watcher] Duplicate skip {}", name);
            archive(file, "duplicate");
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            MultipartFile mf = new MockMultipartFile(
                    name, name, "application/octet‑stream", fis);
            if ("accounts".equals(type))  accountService.importByFile(mf);
            else                          stmtService.importByFile(mf);
            log.info("[Watcher] Import OK {}", name);
            archive(file, "ok");
        } catch (Exception ex) {
            log.error("[Watcher] Import FAIL {}", name, ex);
            archive(file, "fail");
        }
    }

    private void archive(File src, String status) {
        try {
            Path parent = src.toPath().getParent();
            Path targetDir = parent.resolve("archive").resolve(status);
            Files.createDirectories(targetDir);
            String ts = TS_FMT.format(LocalDateTime.now());
            Path dest = targetDir.resolve(ts + "-" + src.getName());
            Files.move(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            log.info("[Watcher] Archived to {}", dest);
        } catch (IOException ioe) {
            log.error("[Watcher] Archive failed for {}", src.getName(), ioe);
        }
    }
}
