
# Backend Assignment 

## Overview

Microservices, as opposed to monolith applications, dictates that you have to divide your application into small services. As part of the Engineering team, you have been given the task to create a microservice for the new domain Transaction.  

## Development  

Using Java, Spring Boot and Gradle, build a microservice that serves the transaction domain with the following details:  

Domain:  
```
transaction: {
“Id” : “f1fb5da5-5418-4ac7”, (UUID)
“accountId” : “d4fb5ta7-8028-5cf8”, (UUID)
“currency” : “GBP”, (String)
“amount”: 234,95, (Decimal)
“description”: “Tesco Holborn Station” (String)
“type”: “CREDIT” (Enum)
}
```

The only available types are “CREDIT” and “DEBIT”.  

## Endpoints  

```
/transactions (GET/POST)
/transactions/{transactionId} (GET/PUT/DELETE)
```

Where the batch retrieve returns a paginated list of items, the retrieve by Id only retrieves a single item.

## Tasks  

Once the transaction is created, we require the endpoint to start a new thread that asynchronously does the following:  

•	Add or subtract the amount (depending on the type) for the transaction, from the total running balance, the initial balance being 0.

## Platform  

Add a dockerfile to be able to build the container for the application. Implement GitLab CI/CD or another CI/CD tool if using GitHub and document deployment.  

## Deliverables  

A GitLab or GitHub repo with at minimum:
1.	The complete source code.
2.	Full test(s) coverage.
3.	Platform folder with dockerfile & CI/CD.
4.	A README outlining the architecture and instructions to build and run the app locally.

# How to Run and Test Application

## Build and Run

`./gradlew build`  
`./gradlew run`

## Test 

`./gradlew test`  

# How to Test Endpoints using Curl

POST: Create transaction  

`curl -XPOST -H "Content-type: application/json" -d '{
    "id":3,
    "accountId":1,
    "currency": "GBP",
    "amount": 250,
    "description": "Description",
    "type": "CREDIT"
}' 'http://localhost:8080/transactions/'`


GET: Retrieve transaction  

`curl -XGET -H "Content-type: application/json" 'http://localhost:8080/transactions/'`

PUT: Update transaction  

`curl -XPUT -H "Content-type: application/json" -d '{
    "id":1,
    "accountId":1,
    "currency": "GBP",
    "amount": 250,
    "description": "Tesco Holborn Station",
    "type": "DEBIT"
}' 'http://localhost:8080/transactions/1'`

Delete: Delete transaction  

`curl -XDELETE -H "Content-type: application/json" 'http://localhost:8080/transactions/1'`



