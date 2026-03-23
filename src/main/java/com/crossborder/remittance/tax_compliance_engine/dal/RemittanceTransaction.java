package com.crossborder.remittance.tax_compliance_engine.dal;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "remittance_transactions")
public class RemittanceTransaction {

    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "base_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "purpose_code", nullable = false, length = 10)
    private String purposeCode;

    @Column(name = "tcs_amount", precision = 15, scale = 2)
    private BigDecimal tcsAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Constructors
    public RemittanceTransaction() {}

    // Lifecycle Hooks
    @PrePersist
    public void prePersist() {
    	if (this.transactionId == null) {
            this.transactionId = UUID.randomUUID();
        }
    	
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.tcsAmount == null) {
            this.tcsAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters & Setters

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPurposeCode() {
        return purposeCode;
    }

    public void setPurposeCode(String purposeCode) {
        this.purposeCode = purposeCode;
    }

    public BigDecimal getTcsAmount() {
        return tcsAmount;
    }

    public void setTcsAmount(BigDecimal tcsAmount) {
        this.tcsAmount = tcsAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}