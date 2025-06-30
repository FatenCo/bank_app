package com.bank.app.lettrage.repository;

import com.bank.app.lettrage.entity.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour gérer les enregistrements des fichiers importés.
 */
public interface ImportJobRepository extends JpaRepository<ImportJob, UUID> {

    /**
     * Vérifie si un fichier avec un certain nom et statut a déjà été importé.
     */
    boolean existsBySourceAndStatus(String source, String status);

    /**
     * Recherche les imports par nom partiel du fichier.
     */
    List<ImportJob> findBySourceContainingIgnoreCase(String sourcePart);

    /**
     * Recherche les imports lancés entre deux dates.
     */
    List<ImportJob> findByStartedAtBetween(LocalDateTime from, LocalDateTime to);

    /**
     * Création rapide d’un job (initialisé à IN_PROGRESS).
     */
    default ImportJob createNew(String source) {
        ImportJob job = new ImportJob(UUID.randomUUID(), source);
        return save(job);
    }

    /**
     * Récupère le dernier job pour un fichier.
     */
    Optional<ImportJob> findTopBySourceOrderByStartedAtDesc(String source);
}
