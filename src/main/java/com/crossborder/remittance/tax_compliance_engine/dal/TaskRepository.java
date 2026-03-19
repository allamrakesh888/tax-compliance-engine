package com.crossborder.remittance.tax_compliance_engine.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Standard CRUD operations are already included!
}