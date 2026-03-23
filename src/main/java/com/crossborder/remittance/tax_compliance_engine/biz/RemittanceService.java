package com.crossborder.remittance.tax_compliance_engine.biz;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crossborder.remittance.tax_compliance_engine.common.RemittanceRequest;
import com.crossborder.remittance.tax_compliance_engine.common.TransactionStatus;
import com.crossborder.remittance.tax_compliance_engine.dal.RemittanceRepository;
import com.crossborder.remittance.tax_compliance_engine.dal.RemittanceTransaction;
import com.crossborder.remittance.tax_compliance_engine.dal.User;
import com.crossborder.remittance.tax_compliance_engine.dal.UserRepository;

//import io.awspring.cloud.sqs.operations.SqsTemplate;

@Service
public class RemittanceService {

    private final RemittanceRepository remittanceRepository;
    private final UserRepository userRepository;
    //private final SqsTemplate sqsTemplate;
    
    // AWS Queue URL or Name from application.yml
    //private static final String COMPLIANCE_QUEUE = "compliance-check-queue";

    public RemittanceService(RemittanceRepository remittanceRepository, UserRepository userRepository
    		//,SqsTemplate sqsTemplate
    		) {
        this.remittanceRepository = remittanceRepository;
        this.userRepository = userRepository;
        //this.sqsTemplate = sqsTemplate;
    }

    @Transactional
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
       /* ComplianceCheckEvent event = new ComplianceCheckEvent(
                savedTx.getTransactionId(),
                savedTx.getUserId(),
                savedTx.getBaseAmount(),
                request.getPurposeCode()
        );
        
        //sqsTemplate.send(COMPLIANCE_QUEUE, event);

        // 4. Return the generated ID for the client
        return savedTx.getTransactionId().toString();*/
        return "";
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
