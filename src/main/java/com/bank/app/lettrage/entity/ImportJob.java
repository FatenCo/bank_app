package com.bank.app.lettrage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente un job d'importation de fichier (accounts ou statements).
 */
@Entity
@Table(name = "import_job")
public class ImportJob {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String source; // Nom du fichier

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "total_records")
    private int totalRecords;

    @Column(name = "processed_records")
    private int processedRecords;

    @Column(nullable = false)
    private String status; // IN_PROGRESS, COMPLETED, FAILED

    @Lob
    @Column(name = "error_message", columnDefinition = "MEDIUMTEXT")
    private String errorMessage;

    public ImportJob() {
        // Pour JPA
    }

    public ImportJob(UUID id, String source) {
        this.id = id;
        this.source = source;
        this.startedAt = LocalDateTime.now();
        this.status = "IN_PROGRESS";
    }

    // --- Getters & Setters ---
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(int processedRecords) {
        this.processedRecords = processedRecords;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // --- Méthodes utilitaires ---
    /**
     * Marquer le job comme terminé avec succès.
     */
    public void complete(int count) {
        this.totalRecords = count;
        this.processedRecords = count;
        this.completedAt = LocalDateTime.now();
        this.status = "COMPLETED";
    }

    /**
     * Marquer le job comme échoué avec message d'erreur.
     */
    public void fail(String message) {
        this.completedAt = LocalDateTime.now();
        this.status = "FAILED";
        this.errorMessage = message;
    }
}
