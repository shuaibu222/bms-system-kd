package com.shuaibu.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SaleItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    private Integer quantity;
    private Double price;

    private Integer returnedQuantity; // Track how many items were returned
    private String returnReason; // Optional: reason for return

    @ManyToOne
    @JoinColumn(name = "sale_id")
    private SaleModel sale;
}
