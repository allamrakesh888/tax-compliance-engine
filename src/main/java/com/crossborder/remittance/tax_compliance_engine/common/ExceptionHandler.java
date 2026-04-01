package com.crossborder.remittance.tax_compliance_engine.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ExceptionHandler {

	public static ResponseEntity<Map<String, String>> handleException(Exception e) {
		Map<String, String> response = new LinkedHashMap<>();

		String mesg = e.getMessage();

		if (mesg.contains("Payload Validation failed")) {
			response.put(Constants.STATUS, HttpStatus.BAD_REQUEST.name());
			response.put(Constants.MESSAGE, mesg);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		} else if (mesg.contains("KYC")) {
			response.put(Constants.STATUS, HttpStatus.FORBIDDEN.name());
			response.put(Constants.MESSAGE, "User KYC is not completed");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		} else if (mesg.contains("USER_NOT_FOUND")) {
			response.put(Constants.STATUS, HttpStatus.NOT_FOUND.name());
			response.put(Constants.MESSAGE, "User not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		response.put(Constants.STATUS, HttpStatus.INTERNAL_SERVER_ERROR.name());
		response.put(Constants.MESSAGE, e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
