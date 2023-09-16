package com.tabcorp.transaction.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name="product")
@Getter
@Setter
public class Product {
    @Id
    @Column(name = "product_code")
    private String productCode;

    private double cost;

    private String status;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<Transaction> transactions;
}
