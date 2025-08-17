package com.bank.app.lettrage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Définit un process (import comptes, import statements, ou réconciliation).
 */
@Getter
@Entity
@Table(name = "process_definition")
public class ProcessDefinition {

    @Setter
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id = UUID.randomUUID();

    @Setter
    @Column(nullable = false, length = 200)
    private String name;

    @Setter
    @Column(length = 1024)
    private String description;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProcessType type;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProcessMode mode;

    @Setter
    @Column(nullable = false)
    private boolean enabled;

    @Setter
    @Column(name = "cron_expression", length = 200)
    private String cronExpression;

    @Setter
    @Column(name = "cron_description", length = 200)
    private String cronDescription;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ProcessDefinition() {
        // pour JPA
    }

    public ProcessDefinition(String name,
                             String description,
                             ProcessType type,
                             ProcessMode mode,
                             boolean enabled,
                             String cronExpression,
                             String cronDescription) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.type = type;
        this.mode = mode;
        this.enabled = enabled;
        this.cronExpression = cronExpression;
        this.cronDescription = cronDescription;
    }

    // --- Getters & Setters ---

    // pas de setter pour createdAt (géré par Hibernate)

    // pas de setter pour updatedAt (géré par Hibernate)
}
