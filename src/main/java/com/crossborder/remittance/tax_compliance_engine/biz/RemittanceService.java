package com.crossborder.remittance.tax_compliance_engine.biz;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.crossborder.remittance.tax_compliance_engine.common.ComplianceCheckEvent;
import com.crossborder.remittance.tax_compliance_engine.common.Constants;
import com.crossborder.remittance.tax_compliance_engine.common.RemittanceRequest;
import com.crossborder.remittance.tax_compliance_engine.common.TransactionStatusResponse;
import com.crossborder.remittance.tax_compliance_engine.common.RemitTransactionStatus;
import com.crossborder.remittance.tax_compliance_engine.dal.RemittanceRepository;
import com.crossborder.remittance.tax_compliance_engine.dal.RemittanceTransaction;
import com.crossborder.remittance.tax_compliance_engine.dal.User;
import com.crossborder.remittance.tax_compliance_engine.dal.UserRepository;

import io.awspring.cloud.sqs.operations.SqsTemplate;

@Service
public class RemittanceService {

	private final RemittanceRepository remittanceRepository;
	private final UserRepository userRepository;
	private final SqsTemplate sqsTemplate;

	private final String complianceQueueName;

	public RemittanceService(RemittanceRepository remittanceRepository, UserRepository userRepository,
			SqsTemplate sqsTemplate, @Value("${app.sqs.compliance-queue}") String complianceQueueName) {
		this.remittanceRepository = remittanceRepository;
		this.userRepository = userRepository;
		this.sqsTemplate = sqsTemplate;
		this.complianceQueueName = complianceQueueName;
	}

	@Transactional(rollbackFor = Exception.class)
	public String initiateRemittance(RemittanceRequest request) throws Exception {

		verifyKyc(request.getUserId());

		// Save to PostgreSQL
		RemittanceTransaction transaction = new RemittanceTransaction();
		transaction.setUserId(request.getUserId());
		transaction.setBaseAmount(request.getAmount());
		transaction.setCurrency(request.getCurrency());
		transaction.setPurposeCode(request.getPurposeCode());
		transaction.setStatus(RemitTransactionStatus.PENDING_COMPLIANCE_CHECK.name());

		RemittanceTransaction savedTx = remittanceRepository.save(transaction);

		// Create event and Publish to SQS
		ComplianceCheckEvent event = new ComplianceCheckEvent(savedTx.getTransactionId(), savedTx.getUserId(),
				savedTx.getBaseAmount(), request.getPurposeCode());
		
		sqsTemplate.send(complianceQueueName, event);

		// Return the generated transaction ID for status tracking
		return savedTx.getTransactionId().toString();
	}

	private void verifyKyc(UUID userId) throws Exception {

		User user = userRepository.findById(userId).orElseThrow(() -> new Exception("USER_NOT_FOUND"));

		// other KYC statues PENDING and REJECTED
		if (!user.getKycStatus().equals("VERIFIED")) {
			throw new Exception("KYC_NOT_COMPLETED");
		}
	}
    
	public TransactionStatusResponse getTransactionStatus(UUID transactionId) {

		RemittanceTransaction transaction = remittanceRepository.findById(transactionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Transaction with ID " + transactionId + " not found"));

		String status = transaction.getStatus();
		String reason = null;
		if (RemitTransactionStatus.REJECTED.toString().equals(status)) {
			reason = transaction.getRejectionReason();
		}

		return new TransactionStatusResponse(transaction.getTransactionId(), transaction.getStatus(), reason);
	}

	public void validateIncomingReqPayload(RemittanceRequest request) throws Exception {

		StringBuilder errorBuilder = new StringBuilder();

		if (request.getUserId() == null) {
			errorBuilder.append(" -- userId field is required");
		}

		if (request.getAmount() == null) {
			errorBuilder.append(" -- amount field is required");
		} else if (request.getAmount().compareTo(new BigDecimal(500)) < 0) {
			errorBuilder.append(" -- Minimum Amount is 500");
		}

		if (request.getCurrency() == null) {
			errorBuilder.append(" -- currency field is required");
		} else if (!Constants.ALLOWED_CURRENCIES.contains(request.getCurrency())) {
			errorBuilder.append(" -- Unsupported currency code, Supported Currencies : USD|EUR");
		}

		if (request.getPurposeCode() == null) {
			errorBuilder.append(" -- purposeCode field is required");
		} else if (!Constants.ALLOWED_PURPOSE_CODES.contains(request.getPurposeCode())) {
			errorBuilder.append(
					" -- Unsupported Purpose code, Supported codes : " + Constants.ALLOWED_PURPOSE_CODES.toString());
		}

		if (!errorBuilder.isEmpty()) {
			errorBuilder.append(" -- Payload Validation failed");
			throw new Exception(errorBuilder.toString());
		}
	}
	
}
