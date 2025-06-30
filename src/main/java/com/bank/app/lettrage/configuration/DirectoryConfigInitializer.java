// src/main/java/com/bank/app/lettrage/config/DirectoryConfigInitializer.java
package com.bank.app.lettrage.configuration;

import com.bank.app.lettrage.entity.DirectoryConfig;
import com.bank.app.lettrage.repository.DirectoryConfigRepository;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DirectoryConfigInitializer {

    private final DirectoryConfigRepository repo;
    private final DirectoryConfigProperties props;

    public DirectoryConfigInitializer(
            DirectoryConfigRepository repo,
            DirectoryConfigProperties props
    ) {
        this.repo = repo;
        this.props = props;
    }

    @PostConstruct
    public void load() {
        // clé "accountsDir"
        repo.findById("accountsDir")
                .map(DirectoryConfig::getValue)
                .ifPresent(props::setAccountsDir);

        // clé "stmtsDir"
        repo.findById("stmtsDir")
                .map(DirectoryConfig::getValue)
                .ifPresent(props::setStmtsDir);
    }
}
