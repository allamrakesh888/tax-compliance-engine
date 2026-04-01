package com.crossborder.remittance.tax_compliance_engine.common;

import java.util.Set;

public class Constants {

	public static final String STATUS = "status";
	public static final String MESSAGE = "message";
	public static final String TRANSACTION_ID = "transactionId";
	public static final Set<String> ALLOWED_CURRENCIES = Set.of("USD", "EUR");
	public static final Set<String> ALLOWED_PURPOSE_CODES = Set.of("S0305-EDUCATION_LOAN",
			"S0305-EDUCATION_SELF_FUNDED", "S0306-TRAVEL_TOURISM", "S0304-MEDICAL_TREATMENT", "S0001-INVESTMENT_ABROAD",
			"S1301-FAMILY_MAINTENANCE");
}
