package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.entity.Account;
import com.bank.app.lettrage.entity.ImportResult;
import com.bank.app.lettrage.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/static/accounts")
@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    /** Import manuel “tout ou rien” */
    @PostMapping
    public ResponseEntity<ImportResult> importManual(@RequestBody Account acct) {
        ImportResult res = service.importManual(acct);
        if (res.getSuccessCount() == 1) {
            return ResponseEntity
                    .created(URI.create("/api/static/accounts/" + acct.getAccountNo()))
                    .body(res);
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(res);
        }
    }

    /** Import par fichier (CSV/Excel) */
    @PostMapping("/upload")
    public ResponseEntity<ImportResult> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult res = service.importFromFile(file);
            return ResponseEntity.ok(res);
        } catch (Exception ex) {
            ImportResult err = new ImportResult(
                    0, 0,
                    List.of(new com.bank.app.lettrage.entity.LogEntry(
                            0, com.bank.app.lettrage.entity.LogEntry.Level.ERROR,
                            "Import échoué : " + ex.getMessage())),
                    AccountService.ALERT_THRESHOLD
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }

    /** Lister tous les comptes */
    @GetMapping
    public List<Account> listAll() {
        return service.findAll();
    }
}
