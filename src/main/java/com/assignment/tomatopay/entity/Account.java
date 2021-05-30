package com.assignment.tomatopay.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * @author Suleyman Yildirim
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "balance")
    @Builder.Default
    private double balance = 0;

    // @OneToMany(targetEntity = Transaction.class, fetch = FetchType.LAZY)
    //@JoinColumn(name = "account_id_pk", insertable = false, updatable = false)

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Transaction> transactions;
}
