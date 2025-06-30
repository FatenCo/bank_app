package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.DirectoryConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectoryConfigRepository
        extends JpaRepository<DirectoryConfig, String> {
}
