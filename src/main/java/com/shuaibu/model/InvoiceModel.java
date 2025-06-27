package com.shuaibu.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class InvoiceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invNum;
    private Long quotationId;
    private Double totalAmount;
    private Double cashPaid;
    private Double cardPaid;
    private Double otherPaid;
    private Double totalPaid; // Computed field
    private Double balanceDue; // Remaining balance
    private String paymentStatus;
    private String paymentMethod;
    private LocalDate invoiceDateTime;
}
