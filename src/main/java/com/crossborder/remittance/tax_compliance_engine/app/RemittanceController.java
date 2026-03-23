package com.crossborder.remittance.tax_compliance_engine.app;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.crossborder.remittance.tax_compliance_engine.biz.RemittanceService;
import com.crossborder.remittance.tax_compliance_engine.common.RemittanceRequest;
import com.crossborder.remittance.tax_compliance_engine.common.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/remittances")
public class RemittanceController {

    private final RemittanceService remittanceService;

    public RemittanceController(RemittanceService remittanceService) {
        this.remittanceService = remittanceService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> processRemittance(@Valid @RequestBody RemittanceRequest request) {
        
    	 Map<String, String> response = new HashMap<>();
    	 
        // Calls the biz layer
        String transactionId = null;
		try {
			transactionId = remittanceService.initiateRemittance(request);
		} catch (Exception e) {
			return ExceptionHandler.handleException(e);
		}

        // Return 202 Accepted
       
        response.put("transactionId", transactionId);
        response.put("status", "ACCEPTED");
        response.put("message", "Transaction is pending compliance verification.");

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}