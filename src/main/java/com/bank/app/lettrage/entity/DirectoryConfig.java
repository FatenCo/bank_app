package com.bank.app.lettrage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "directory_config")
public class DirectoryConfig {

    @Id
    @Column(name = "config_key", length = 100)
    private String key;

    @Column(name = "config_value", length = 1024, nullable = false)
    private String value;

    public DirectoryConfig() {}
    public DirectoryConfig(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
