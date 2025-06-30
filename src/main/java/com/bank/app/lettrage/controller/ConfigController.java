package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.configuration.DirectoryConfigProperties;
import com.bank.app.lettrage.entity.DirectoryConfig;
import com.bank.app.lettrage.repository.DirectoryConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config/directories")
public class ConfigController {

    private final DirectoryConfigRepository repo;
    private final DirectoryConfigProperties props;

    public ConfigController(
            DirectoryConfigRepository repo,
            DirectoryConfigProperties props
    ) {
        this.repo = repo;
        this.props = props;
    }

    public static class DirectoryConfigDto {
        private String accountsDir;
        private String stmtsDir;
        public DirectoryConfigDto() {}
        public DirectoryConfigDto(String a, String s) {
            this.accountsDir = a;
            this.stmtsDir    = s;
        }
        public String getAccountsDir() { return accountsDir; }
        public void setAccountsDir(String a) { this.accountsDir = a; }
        public String getStmtsDir() { return stmtsDir; }
        public void setStmtsDir(String s) { this.stmtsDir = s; }
    }

    @GetMapping
    public ResponseEntity<DirectoryConfigDto> getConfig() {
        return ResponseEntity.ok(new DirectoryConfigDto(
                props.getAccountsDir(),
                props.getStmtsDir()
        ));
    }

    @PutMapping
    public ResponseEntity<Void> updateConfig(@RequestBody DirectoryConfigDto dto) {
        // Mettre à jour la BD
        repo.save(new DirectoryConfig("accountsDir", dto.getAccountsDir()));
        repo.save(new DirectoryConfig("stmtsDir",    dto.getStmtsDir()));
        // Mettre à jour le bean en mémoire pour la session courante
        props.setAccountsDir(dto.getAccountsDir());
        props.setStmtsDir(dto.getStmtsDir());
        return ResponseEntity.noContent().build();
    }
}
