// src/main/java/com/bank/app/lettrage/repository/ProcessDefinitionRepository.java
package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.ProcessDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessDefinitionRepository extends JpaRepository<ProcessDefinition, UUID> {
    // méthodes standard CRUD héritées
}
