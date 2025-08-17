
package com.bank.app.lettrage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "process_execution")
public class ProcessExecution {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id = UUID.randomUUID();

    @JsonIgnore // Empêche la sérialisation de la relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", nullable = false)
    private ProcessDefinition definition;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(length = 1024)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessExecStatus status;

    public ProcessExecution() {
        // pour JPA
    }

    public ProcessExecution(ProcessDefinition definition, LocalDateTime startTime, ProcessExecStatus status) {
        this.id = UUID.randomUUID();
        this.definition = definition;
        this.startTime = startTime;
        this.status = status;
    }

    // --- Getters & Setters ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProcessDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ProcessDefinition definition) {
        this.definition = definition;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ProcessExecStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessExecStatus status) {
        this.status = status;
    }
}
