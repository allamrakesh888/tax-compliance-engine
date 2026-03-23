package com.crossborder.remittance.tax_compliance_engine.common;

import java.math.BigDecimal;
import java.util.UUID;

//The event payload we will push to SQS

public record ComplianceCheckEvent(UUID transactionId, UUID userId, BigDecimal amount, String purposeCode) {
}
