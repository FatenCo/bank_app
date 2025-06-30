// src/main/java/com/bank/app/lettrage/entity/ProcessDefinition.java
package com.bank.app.lettrage.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "process_definition")
public class ProcessDefinition {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessMode mode;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "cron_description")
    private String cronDescription;

    // --- AJOUT pour timestamps ---
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- getters / setters ---

    public ProcessDefinition() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ProcessType getType() { return type; }
    public void setType(ProcessType type) { this.type = type; }

    public ProcessMode getMode() { return mode; }
    public void setMode(ProcessMode mode) { this.mode = mode; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public String getCronDescription() { return cronDescription; }
    public void setCronDescription(String cronDescription) { this.cronDescription = cronDescription; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
