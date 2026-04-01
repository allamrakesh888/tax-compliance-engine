package com.crossborder.remittance.tax_compliance_engine.dal;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

	@Id
	@Column(name = "user_id", updatable = false, nullable = false)
	private UUID userId;

	@Column(name = "email", nullable = false, unique = true, length = 255)
	private String email;

	@Column(name = "kyc_status", nullable = false, length = 50)
	private String kycStatus = "PENDING";

	@Column(name = "account_balance", nullable = false, precision = 15, scale = 2)
	private BigDecimal accountBalance = BigDecimal.ZERO;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private OffsetDateTime updatedAt;

	public User() {
	}

	@PrePersist
	public void prePersist() {
		if (this.userId == null) {
			this.userId = UUID.randomUUID();
		}

		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getKycStatus() {
		return kycStatus;
	}

	public void setKycStatus(String kycStatus) {
		this.kycStatus = kycStatus;
	}

	public BigDecimal getAccountBalance() {
		return accountBalance;
	}

	public void setAccountBalance(BigDecimal accountBalance) {
		this.accountBalance = accountBalance;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
