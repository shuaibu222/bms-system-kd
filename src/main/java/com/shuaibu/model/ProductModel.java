package com.shuaibu.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ProductModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double price;
    private Integer quantity;
    private Integer lowQuantityAlert;
    private Integer soldQuantity;
    private LocalDate expiryDate;
    private String nafdac;
    private LocalDate lowStockDate;

    @Transient
    private String expiryWarning;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Transient
    public String getExpiryWarning() {
        if (expiryDate == null)
            return null;

        LocalDate today = LocalDate.now();
        long daysToExpiry = ChronoUnit.DAYS.between(today, expiryDate);

        if (daysToExpiry < 0) {
            return "❌ Expired";
        } else if (daysToExpiry <= 30) {
            return "⚠️ Expiring Soon (30 days)";
        } else if (daysToExpiry <= 90) {
            return "🟠 Expires in 3 Months";
        } else if (daysToExpiry <= 180) {
            return "🔵 Expires in 6 Months";
        }
        return null; // Beyond 6 months, no warning
    }

}
