package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.entity.ImportJob;
import com.bank.app.lettrage.entity.StatementEntry;
import com.bank.app.lettrage.service.StatementImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/stmts")
public class StatementController {

    private final StatementImportService service;

    public StatementController(StatementImportService service) {
        this.service = service;
    }

    @PostMapping("/manual")
    public ResponseEntity<ImportJob> importManual(@RequestBody List<StatementEntry> entries) {
        return ResponseEntity.ok(service.importManual(entries));
    }

    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<ImportJob>> upload(@RequestParam("file") MultipartFile file) {
        return service.importByFile(file)
                .thenApply(job -> ResponseEntity.accepted().body(job));
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<ImportJob> status(@PathVariable UUID id) {
        return service.fetchJob(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/file/{name}")
    public ResponseEntity<List<ImportJob>> searchByFile(@PathVariable String name) {
        return ResponseEntity.ok(service.searchByFileName(name));
    }

    @GetMapping("/search/date/{date}")
    public ResponseEntity<List<ImportJob>> searchByDate(@PathVariable String date) {
        return ResponseEntity.ok(service.searchByDate(date));
    }

    @GetMapping("/entries")
    public ResponseEntity<List<StatementEntry>> listEntries() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/entries/{id}")
    public ResponseEntity<StatementEntry> getOne(@PathVariable UUID id) {
        return service.getOne(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/entries/{id}")
    public ResponseEntity<StatementEntry> update(
            @PathVariable UUID id,
            @RequestBody StatementEntry updated
    ) {
        return service.update(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
