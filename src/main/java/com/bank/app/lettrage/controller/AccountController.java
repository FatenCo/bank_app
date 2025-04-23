package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.entity.Account;
import com.bank.app.lettrage.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@RestController
@RequestMapping("/api/static/accounts")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;

    // 1️⃣ Ajout manuel
    @PostMapping
    public ResponseEntity<Account> createManual(@RequestBody Account acct) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveManual(acct));
    }

    // 2️⃣ Import fichier (Excel ou CSV)
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            List<String> logs = service.importFromFile(file);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(List.of("Erreur d'import : " + e.getMessage()));
        }
    }
}
