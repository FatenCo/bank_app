// src/main/java/com/bank/app/lettrage/entity/ProcessExecution.java
package com.bank.app.lettrage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "process_execution")
public class ProcessExecution {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "definition_id")
    private ProcessDefinition definition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessExecStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Lob
    private String message;

    public ProcessExecution() {
        this.startTime = LocalDateTime.now();
    }

    // -- getters & setters --

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ProcessDefinition getDefinition() { return definition; }
    public void setDefinition(ProcessDefinition definition) { this.definition = definition; }

    public ProcessExecStatus getStatus() { return status; }
    public void setStatus(ProcessExecStatus status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
