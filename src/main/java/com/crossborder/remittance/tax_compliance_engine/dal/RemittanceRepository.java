package com.crossborder.remittance.tax_compliance_engine.dal;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RemittanceRepository extends JpaRepository<RemittanceTransaction, UUID> {
}