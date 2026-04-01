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

	@Column(name = "base_amount_inr", nullable = false, precision = 15, scale = 2)
	private BigDecimal baseAmountInr;

	@Column(name = "target_currency", nullable = false, length = 3)
	private String targetCurrency;

	@Column(name = "purpose_code", nullable = false, length = 30)
	private String purposeCode;

	@Column(name = "tcs_amount_inr", precision = 15, scale = 2)
	private BigDecimal tcsAmountInr = BigDecimal.ZERO;

	@Column(name = "status", nullable = false, length = 50)
	private String status;

	@Column(name = "created_at", updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at")
	private OffsetDateTime updatedAt;

	@Column(name = "rejection_reason", nullable = false, length = 255)
	private String rejectionReason;

	public RemittanceTransaction() {
	}

	@PrePersist
	public void prePersist() {
		if (this.transactionId == null) {
			this.transactionId = UUID.randomUUID();
		}

		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;

		if (this.tcsAmountInr == null) {
			this.tcsAmountInr = BigDecimal.ZERO;
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

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
		return baseAmountInr;
	}

	public void setBaseAmount(BigDecimal baseAmountInr) {
		this.baseAmountInr = baseAmountInr;
	}

	public String getCurrency() {
		return targetCurrency;
	}

	public void setCurrency(String targetCurrency) {
		this.targetCurrency = targetCurrency;
	}

	public String getPurposeCode() {
		return purposeCode;
	}

	public void setPurposeCode(String purposeCode) {
		this.purposeCode = purposeCode;
	}

	public BigDecimal getTcsAmount() {
		return tcsAmountInr;
	}

	public void setTcsAmount(BigDecimal tcsAmountInr) {
		this.tcsAmountInr = tcsAmountInr;
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

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
}