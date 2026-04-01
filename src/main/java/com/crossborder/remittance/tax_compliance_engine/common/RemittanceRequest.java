package com.crossborder.remittance.tax_compliance_engine.common;

import java.math.BigDecimal;
import java.util.UUID;

public class RemittanceRequest {

	private UUID userId;
	private BigDecimal amount;
	private String currency;
	private String purposeCode;

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
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
}
