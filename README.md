**1. Cross-Border Remittance & Tax Compliance Engine**   
   
A Distributed, Event-driven, Cloud-native, Microservices-based backend system built on AWS EKS to process international money transfers and asynchronously calculate tax compliance (TCS) using Spring Boot, Amazon RDS (PostgreSQL), Amazon SQS, and AWS Lambda.  
   
The System processes outward international money transfers, with real-time tracking of user-specific annual limits to enforce regulatory compliance and calculate applicable taxes such as Tax Collected at Source (TCS) beyond the ₹10 lakh threshold based on LRS purpose codes (Liberalised Remittance Scheme)
<br><br>    
        
**2. High-Level Architecture (HLD) & Traffic Flow**

<img width="1800" height="1473" alt="AwsArchitectureDiagMT" src="https://github.com/user-attachments/assets/be387c01-c631-4aa8-a5a2-76b171c38b92" />

This system implements a strict Defense-in-Depth network topology. All compute resources and databases are completely isolated within Private Subnets, while a public Network Load Balancer (NLB) acts as the single point of ingress.   

The architecture handles two primary traffic flows:   

**Flow 1: Asynchronous Remittance Initiation**  
&emsp; This flow ensures that the API remains highly available and never drops a transaction, even if the tax calculation engine experiences downtime.

 > **1. Request Ingress:** The Client App initiates a POST request to the public Network Load Balancer (NLB).  

 > **2. Internal Routing:** The NLB routes the traffic to the Spring Boot Pods running on EKS worker nodes inside the isolated Private Subnet.  

 > **3.State Persistence:** The Spring Boot API immediately saves the transaction in the Amazon RDS PostgreSQL database with a status of PENDING.  

 > **4.Event Publishing:** The API pushes a transaction event out through the NAT Gateway to the Amazon SQS Main Queue.  

 > **5.Tax Calculation:** The SQS event triggers the AWS Lambda function (also located in the Private Subnet).    

 > **6.Ledger Update:** The Lambda function calculates the Tax Collected at Source (TCS) and securely connects to the RDS instance to update the final ledger and mark the transaction as COMPLETED.  

**Flow 2: Synchronous Status Polling**  
&emsp;This flow allows clients to securely check the real-time status of their cross-border transfers.

> **1. Status Query:** The Client App sends a GET request to the NLB with the specific Transaction ID.

> **2. Internal Routing:** The NLB forwards the request to the Spring Boot API.

> **3. Synchronous Read:** The API performs a secure read operation directly from the Amazon RDS instance.

> **4. Response:** The API returns the exact status (PENDING, COMPLETED, or FAILED_TAX_COMPLIANCE) back to the client.
