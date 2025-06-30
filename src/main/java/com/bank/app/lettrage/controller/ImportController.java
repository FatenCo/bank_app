package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.entity.AccountEntry;
import com.bank.app.lettrage.entity.ImportJob;
import com.bank.app.lettrage.service.AccountImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/accounts")
public class ImportController {

    private final AccountImportService service;

    public ImportController(AccountImportService service) {
        this.service = service;
    }

    // ======= IMPORT & JOBS =======

    /**
     * POST /api/accounts/manual
     * Import manuel : prend un JSON array d'AccountEntry.
     */
    @PostMapping("/manual")
    public ResponseEntity<ImportJob> importManual(@RequestBody List<AccountEntry> entries) {
        ImportJob job = service.importManual(entries);
        return ResponseEntity.ok(job);
    }

    /**
     * POST /api/accounts/upload
     * Import par fichier (CSV ou Excel), asynchrone.
     */
    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<ImportJob>> upload(@RequestParam("file") MultipartFile file) {
        return service.importByFile(file)
                .thenApply(job -> ResponseEntity.accepted().body(job));
    }

    /**
     * GET /api/accounts/status/{id}
     * Récupérer le statut d'un job d'import.
     */
    @GetMapping("/status/{id}")
    public ResponseEntity<ImportJob> status(@PathVariable UUID id) {
        return service.fetchJob(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/accounts/search/file/{name}
     * Rechercher les jobs dont le nom de fichier contient {name}.
     */
    @GetMapping("/search/file/{name}")
    public ResponseEntity<List<ImportJob>> searchByFileName(@PathVariable String name) {
        List<ImportJob> jobs = service.searchJobsByFileName(name);
        return ResponseEntity.ok(jobs);
    }

    /**
     * GET /api/accounts/search/date/{date}
     * Rechercher les jobs démarrés à la date YYYY-MM-DD.
     */
    @GetMapping("/search/date/{date}")
    public ResponseEntity<List<ImportJob>> searchByDate(@PathVariable String date) {
        List<ImportJob> jobs = service.searchJobsByDate(date);
        return ResponseEntity.ok(jobs);
    }

    // ======= CRUD AccountEntry =======

    /**
     * GET /api/accounts/entries
     * Lister tous les AccountEntry.
     */
    @GetMapping("/entries")
    public ResponseEntity<List<AccountEntry>> listEntries() {
        return ResponseEntity.ok(service.listEntries());
    }

    /**
     * GET /api/accounts/entries/{id}
     * Récupérer un AccountEntry par ID.
     */
    @GetMapping("/entries/{id}")
    public ResponseEntity<AccountEntry> getEntry(@PathVariable UUID id) {
        return service.getEntry(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/accounts/entries/{id}
     * Mettre à jour un AccountEntry existant.
     */
    @PutMapping("/entries/{id}")
    public ResponseEntity<AccountEntry> updateEntry(
            @PathVariable UUID id,
            @RequestBody AccountEntry updated
    ) {
        return service.updateEntry(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/accounts/entries/{id}
     * Supprimer un AccountEntry par ID.
     */
    @DeleteMapping("/entries/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable UUID id) {
        boolean deleted = service.deleteEntry(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
