## 1. Cross-Border Remittance & Tax Compliance Engine     
   
- A Distributed, Event-driven, Cloud-native, Microservices-based backend system built on AWS EKS to process international money transfers and asynchronously calculate tax compliance (TCS) using Spring Boot, Amazon RDS (PostgreSQL), Amazon SQS, and AWS Lambda.  
   
- The System processes outward international money transfers, with real-time tracking of user-specific annual limits to enforce regulatory compliance and calculate applicable taxes such as Tax Collected at Source (TCS) beyond the ₹10 lakh threshold based on LRS purpose codes (Liberalised Remittance Scheme)
<br><br>    
        
## 2. High-Level Architecture (HLD) & Traffic Flow  

<img width="1800" height="1473" alt="AwsArchitectureDiagMT" src="https://github.com/user-attachments/assets/be387c01-c631-4aa8-a5a2-76b171c38b92" />   
    
<br>    
       
This system implements a strict Multi Layered Protection network topology. All compute resources and databases are completely isolated within Private Subnets, while a public Network Load Balancer (NLB) acts as the single point of ingress.   

The architecture handles two primary traffic flows:   

**Flow 1: Asynchronous Remittance Initiation**  
&emsp; This flow ensures that the API remains highly available and never drops a transaction, even if the tax calculation engine(lambda) experiences downtime.

<details open>     
<summary>Click to collapse flow details</summary>        
<br>   
   
 > **1. Request Ingress:** The Client App initiates a POST request to the public Network Load Balancer (NLB).  

 > **2. Internal Routing:** The NLB routes the traffic to the Spring Boot Pods running on EKS worker nodes inside the isolated Private Subnet.  

 > **3.State Persistence:** The Spring Boot API immediately saves the transaction in the Amazon RDS PostgreSQL database with a status of PENDING_COMPLIANCE_CHECK.  

 > **4.Event Publishing:** The API pushes a transaction event out through the NAT Gateway to the Amazon SQS Main Queue.  

 > **5.Tax Calculation:** The SQS event triggers the AWS Lambda function (also located in the Private Subnet).    

 > **6.Ledger Update:** The Lambda function calculates the Tax Collected at Source (TCS) and securely connects to the RDS instance to update the final ledger and mark the transaction as APPROVED.  
</details>
<br>

**Flow 2: Synchronous Status Polling**  
&emsp;This flow allows clients to check the real-time status of their cross-border transfers.

<details open>     
<summary>Click to collapse flow details</summary>        
<br>  
   
> **1. Status Query:** The Client App sends a GET request to the NLB with the specific Transaction ID.

> **2. Internal Routing:** The NLB forwards the request to the Spring Boot API.

> **3. Synchronous Read:** The API performs a secure read operation directly from the Amazon RDS instance.

> **4. Response:** The API returns the exact status (PENDING_COMPLIANCE_CHECK, APPROVED or REJECTED(with Rejection Reason)) back to the client.
</details>      

### 🔗 Related Repositories

This project follows a decoupled Microservice architecture. While this repository contains the core EKS Spring Boot API and infrastructure definitions, the tax calculation engine is maintained and deployed separately.

* **[Tax Calculator Lambda Repo↗](https://github.com/allamrakesh888/tax-compliance-lambda)** - The serverless Java function that consumes SQS events and updates the PostgreSQL ledger and Remittance Transaction.
  
<br> 

 ## 3. End-to-End Execution flow
[Click here to watch the E2E project demo](https://youtu.be/bDWPFlSpqG8) [youtube video link]

<br>

## 4. Key Engineering Decisions
<details open>     
<summary>Click to collapse decisions</summary>        
<br>   
   
> **1. Multi Layered Protection Networking:** All the heavy lifters (EKS nodes, Lambda, database) are hidden away in isolated Private Subnets. The only way in from the outside world is through a super-fast Network Load Balancer (NLB) acting as the front door.
    
> **2. Event-Driven Decoupling:** Used Amazon SQS to separate the fast Spring Boot API from the heavy tax calculation Lambda. Even if the tax engine crashes, the API stays up, saves the transaction as pending, and queues it safely.
 
> **3. IAM Least Privilege:** Pushed the Lambda function directly into the custom VPC and locked down the database security groups. The database only accepts traffic from specific, pre-approved EKS nodes and the Lambda—nobody else gets a pass.

</details>
<br>  

## 5. Tech Stack & Infrastructure

> **Core Application:** Java 21, Spring Boot, Spring Data JPA (Hibernate/JPA).

> **Database:** Amazon RDS (PostgreSQL).

> **Cloud Infrastructure:** AWS EKS (Kubernetes), Amazon SQS, AWS Lambda, VPC (NAT Gateways, Public/Private Subnets).

> **Infrastructure as Code (IaC):** eksctl, standard Kubernetes manifests (deployment.yaml, service.yaml)
<br>

## 6. API Contracts (Payloads & Endpoints)
The API is designed around RESTful principles, utilizing standard HTTP status codes to reflect the asynchronous nature of the remittance engine.  
**1. Initiate Remittance**
POST /api/v1/remittances

Accepts a new cross-border transfer request. The system instantly persists the transaction to the PostgreSQL remittance table and publishes an event to SQS for downstream tax processing.   

Request Payload:
```
{
    "userId":"9a63884b-3f79-4b04-ac4c-7b45b477d5ff",
    "amount":"100000",
    "currency":"USD",
    "purposeCode":"S0305-EDUCATION_LOAN"
}
```


Response Payload (202 Accepted):
```
{
    "status": "ACCEPTED",
    "transactionId": "e85a0bf7-4e3c-4afc-a1fc-06556fc2fca0",
    "message": "Transaction state : PENDING_COMPLIANCE_CHECK"
}
```

**2. Get Transaction Status**
GET /api/v1/transaction/{transactionId}/status

A synchronous endpoint used by the client application to poll the real-time status of their transfer after the AWS Lambda function processes the compliance rules.   

Response Payload (200 OK):
```
{
    "transactionId": "4feed174-af08-4e9b-96c2-e389cdf15189",
    "status": "APPROVED"
}
```
<br>

## 7. Local Setup & Execution
As this project is built on AWS environment with strict networking rules, you cannot connect to the database directly over the public internet. Follow these steps to tunnel into the private subnet and run the application locally.   
<br>
**Prerequisites**      
Ensure you have the following installed on your local machine:     

  - Java 21 & Maven 3.8+
  - AWS CLI (configured with your IAM user credentials)
  - kubectl (configured to communicate with your EKS cluster)  

<details>
<summary><b>Click to expand: Environment Variables (.env)</b></summary>

Create an <code>application-local.properties</code> or configure your IDE environment variables with the following:  

AWS_REGION=us-east-1  
SQS_QUEUE_NAME=ComplianceCheckQueue  

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/remittance_db    
SPRING_DATASOURCE_USERNAME=your_master_username    
SPRING_DATASOURCE_PASSWORD=your_super_secret_password    
</details>    

**Step 1: Establish the Database Tunnel (Proxy Pod)**  
To connect to the Amazon RDS instance hidden inside the Private Subnet, we use a lightweight proxy pod deployed in the EKS cluster to securely port-forward traffic.   
- kubectl run pg-tunnel --image=alpine/socat --port=5432 -- tcp-listen:5432,fork,reuseaddr tcp-connect:{AwsRdsHost}:5432
- kubectl port-forward pod/pg-tunnel 5432:5432

**Step 2: Run the Spring Boot Application**   
With the tunnel open, open a second terminal window and start the application:  

mvn spring-boot:run -Dspring-boot.run.profiles=local

**Step 3: Test the Flow**   
Once the server starts on port 8080, you can hit the API locally.    
The Spring Boot application will successfully save the transaction to the remote AWS RDS instance (via your tunnel) and 
push the event out to the AWS SQS queue.

To safely shut down the local server and close the database tunnel, press <kbd>Ctrl</kbd> + <kbd>C</kbd> in your terminal windows. 
     
<br>

## 8. Cloud Infrastructure Deployment
This project uses eksctl and the AWS CLI to provision the required cloud infrastructure. If you wish to replicate this environment in your own AWS account, follow these steps in order.  

**1. Provision the EKS Cluster & Custom VPC**    
We use eksctl to automatically generate the VPC, public/private subnets, NAT Gateways, and the worker node IAM roles.

Create the cluster using the declarative config file
```
eksctl create cluster -f infrastructure/cluster.yaml
```
**2. Provision the Amazon SQS Queue**    
Once the cluster is up, create the standard SQS queue that will act as the event bus between the Spring Boot API and the Lambda function.
```
aws sqs create-queue \
    --queue-name ComplianceCheckQueue \
    --region us-east-1
```
**3. Provision the Private PostgreSQL Database**     
Because the database must reside in the private subnet created by eksctl in Step 1, it is provisioned via the AWS Console with the following strict parameters:

- VPC: Select the newly created tax-compliance-cluster VPC.
- Subnet Group: Assign to the Private Subnets.
- Public Access: Set to No.
- Security Group: Create an inbound rule allowing Port 5432 only from the EKS Node Security Group ID.

**4. Deploy the Proxy Tunnel**    
Deploy the lightweight socat proxy pod into the cluster. This creates the secure bridge into the private subnet for local pgAdminTool usage.  
```
kubectl apply -f infrastructure/pg-tunnel-deployment.yaml
```
**5. Deploy the Application**  
Finally, apply the Kubernetes manifests to deploy the Spring Boot API and expose it via the Network Load Balancer.

> Create the native Kubernetes secrets for DB credentials
```
kubectl create secret generic rds-credentials \
  --from-literal=username='postgres_user' \
  --from-literal=password='super_secret_password'
```
>  Deploy the application and the NLB service
```
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```
