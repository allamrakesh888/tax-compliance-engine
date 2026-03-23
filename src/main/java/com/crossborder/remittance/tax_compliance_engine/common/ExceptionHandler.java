package com.crossborder.remittance.tax_compliance_engine.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ExceptionHandler {

	
	public static  ResponseEntity<Map<String, String>> handleException(Exception e){
		Map<String, String> response = new HashMap<>();
		
		String mesg  = e.getMessage();
		
		if(mesg.contains("KYC")) {
			 response.put("message", "User KYC is not completed");
			 return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}
		else if(mesg.contains("USER_NOT_FOUND")) {
			 response.put("message", "User not found");
			 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		
		response.put("message", e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
