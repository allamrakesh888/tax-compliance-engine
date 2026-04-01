package com.crossborder.remittance.tax_compliance_engine.common;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionStatusResponse {

	UUID transactionId;
	String status;
	String rejectionReason;

	public TransactionStatusResponse(UUID transactionId, String status, String rejectionReason) {
		this.transactionId = transactionId;
		this.status = status;
		this.rejectionReason = rejectionReason;
	}

	public UUID getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(UUID transactionId) {
		this.transactionId = transactionId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
}