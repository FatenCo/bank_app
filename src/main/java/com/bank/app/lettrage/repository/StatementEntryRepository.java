package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.StatementEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface StatementEntryRepository extends JpaRepository<StatementEntry, UUID> {
    // CRUD standard
}
