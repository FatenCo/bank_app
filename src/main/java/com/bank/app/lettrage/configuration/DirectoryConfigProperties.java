package com.bank.app.lettrage.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "watch")
public class DirectoryConfigProperties {
    private String accountsDir;
    private String stmtsDir;
    private String archiveAccountsDir;
    private String archiveStmtsDir;

    public String getAccountsDir() {
        return accountsDir;
    }
    public void setAccountsDir(String accountsDir) {
        this.accountsDir = accountsDir;
    }

    public String getStmtsDir() {
        return stmtsDir;
    }
    public void setStmtsDir(String stmtsDir) {
        this.stmtsDir = stmtsDir;
    }
    public String getArchiveAccountsDir() {
        return archiveAccountsDir;
    }
    public void setArchiveAccountsDir(String archiveAccountsDir) {
        this.archiveAccountsDir = archiveAccountsDir;
    }
    public String getArchiveStmtsDir() {
        return archiveStmtsDir;
    }
    public void setArchiveStmtsDir(String archiveStmtsDir) {
        this.archiveStmtsDir = archiveStmtsDir;
    }
}
