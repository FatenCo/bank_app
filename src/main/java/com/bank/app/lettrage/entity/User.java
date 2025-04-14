package com.bank.app.lettrage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPasswordChange;

    private int failedAttempts;
    private boolean isLocked;
}
