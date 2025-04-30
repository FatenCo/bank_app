package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountNo(String accountNo);
    boolean existsByImportFileNameAndImportDate(String fileName, LocalDate date);
}
