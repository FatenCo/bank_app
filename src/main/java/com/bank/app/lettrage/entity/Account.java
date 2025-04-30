package com.bank.app.lettrage.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "accounts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_no", nullable = false)
    private String accountNo;
    @Column(name = "short_name")
    private String shortName;
    private String mnemonic;
    private String accountOfficer;
    private String product;
    private String currency;
    @Column(name = "customer_id")
    private String customerId;
    @Column(name = "ma_code")
    private String maCode;
    @Column(name = "account_type")
    private String accountType;
    @Column(name = "co_code")
    private String coCode;

    // Nouveaux champs pour la saisie manuelle
    @Column(name = "import_file_name", nullable = false)
    private String importFileName;

    @Column(name = "import_date", nullable = false)
    private LocalDate importDate;
}
