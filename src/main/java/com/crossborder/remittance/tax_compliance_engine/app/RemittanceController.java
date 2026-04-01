package com.crossborder.remittance.tax_compliance_engine.app;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.crossborder.remittance.tax_compliance_engine.biz.RemittanceService;
import com.crossborder.remittance.tax_compliance_engine.common.RemittanceRequest;
import com.crossborder.remittance.tax_compliance_engine.common.TransactionStatusResponse;
import com.crossborder.remittance.tax_compliance_engine.common.Constants;
import com.crossborder.remittance.tax_compliance_engine.common.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class RemittanceController {

	private final RemittanceService remittanceService;

	public RemittanceController(RemittanceService remittanceService) {
		this.remittanceService = remittanceService;
	}

	@PostMapping("/remittances")
	public ResponseEntity<Map<String, String>> processRemittance(@RequestBody RemittanceRequest request) {

		String transactionId = null;
		try {
			remittanceService.validateIncomingReqPayload(request);
			transactionId = remittanceService.initiateRemittance(request);
		} catch (Exception e) {
			return ExceptionHandler.handleException(e);
		}

		Map<String, String> response = new LinkedHashMap<>();
		response.put(Constants.STATUS, "ACCEPTED");
		response.put(Constants.TRANSACTION_ID, transactionId);
		response.put(Constants.MESSAGE, "Transaction state : PENDING_COMPLIANCE_CHECK");

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response); // Return 202 Accepted
	}

	@GetMapping("/transaction/{transactionId}/status")
	public ResponseEntity<TransactionStatusResponse> getTransactionStatus(@PathVariable UUID transactionId) {
		TransactionStatusResponse response = remittanceService.getTransactionStatus(transactionId);
		return ResponseEntity.ok(response);
	}
}