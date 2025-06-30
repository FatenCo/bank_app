package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.AccountEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntry, UUID> {
    // CRUD standard hérité de JpaRepository
}
