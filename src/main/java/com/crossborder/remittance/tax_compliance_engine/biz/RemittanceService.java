package com.crossborder.remittance.tax_compliance_engine.biz;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crossborder.remittance.tax_compliance_engine.common.ComplianceCheckEvent;
import com.crossborder.remittance.tax_compliance_engine.common.RemittanceRequest;
import com.crossborder.remittance.tax_compliance_engine.common.TransactionStatus;
import com.crossborder.remittance.tax_compliance_engine.dal.RemittanceRepository;
import com.crossborder.remittance.tax_compliance_engine.dal.RemittanceTransaction;
import com.crossborder.remittance.tax_compliance_engine.dal.User;
import com.crossborder.remittance.tax_compliance_engine.dal.UserRepository;

import io.awspring.cloud.sqs.operations.SqsTemplate;

//import io.awspring.cloud.sqs.operations.SqsTemplate;

@Service
public class RemittanceService {

    private final RemittanceRepository remittanceRepository;
    private final UserRepository userRepository;
    private final SqsTemplate sqsTemplate;
    
    private final String complianceQueueName;
    //= "http://localhost.localstack.cloud:4566/queue/us-east-1/000000000000/test-queue";

    public RemittanceService(RemittanceRepository remittanceRepository, UserRepository userRepository,
    		SqsTemplate sqsTemplate, @Value("${app.sqs.compliance-queue}") String complianceQueueName) {
        this.remittanceRepository = remittanceRepository;
        this.userRepository = userRepository;
        this.sqsTemplate = sqsTemplate;
		this.complianceQueueName = complianceQueueName;
    }

    @Transactional(rollbackFor = Exception.class)
    public String initiateRemittance(RemittanceRequest request) throws Exception {
        // 1. KYC
        verifyKyc(request.getUserId());

        // 2. State Management: Save to PostgreSQL
        RemittanceTransaction transaction = new RemittanceTransaction();
        transaction.setUserId(request.getUserId());
        transaction.setBaseAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setPurposeCode(request.getPurposeCode());
        transaction.setStatus(TransactionStatus.PENDING_COMPLIANCE_CHECK.name());
        
        RemittanceTransaction savedTx = remittanceRepository.save(transaction);
        
        // 3. Decoupling: Publish to Amazon SQS
        //TODO : add user email in sqs event for ses notification
        ComplianceCheckEvent event = new ComplianceCheckEvent(
                savedTx.getTransactionId(),
                savedTx.getUserId(),
                savedTx.getBaseAmount(),
                request.getPurposeCode()
        );
        
        sqsTemplate.send(complianceQueueName, event);

        // 4. Return the generated ID for the client
        return savedTx.getTransactionId().toString();
    }

    private void verifyKyc(UUID userId) throws Exception {
       
    	 User user = userRepository.findById(userId)
                 .orElseThrow(() -> new Exception("USER_NOT_FOUND"));
    	 
    	 //other statues PENDING and REJECTED
         if (!user.getKycStatus().equals("VERIFIED")) {
             throw new Exception("KYC_NOT_COMPLETED");
         }
    }
}
