// src/main/java/com/bank/app/lettrage/repository/ProcessExecutionRepository.java
package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.ProcessExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessExecutionRepository extends JpaRepository<ProcessExecution, UUID> {
    List<ProcessExecution> findByDefinitionIdOrderByStartTimeDesc(UUID definitionId);
}

