# Technology: 

The project should be implemented in Java.
Use spring boot for api, use docker.

# Wallet Service

A robust and secure microservice for managing digital wallets and financial transactions.

## Overview

This service provides a complete solution for managing digital wallets, including creating wallets, handling deposits and withdrawals, and facilitating transfers between users. The service is designed with high availability and full transaction traceability in mind.

## Features

### Functional Requirements

- **Create Wallet**: Create new wallets for users
- **Retrieve Balance**: Get the current balance of a user's wallet
- **Retrieve Historical Balance**: View wallet balance at any point in time
- **Deposit Funds**: Add money to user wallets
- **Withdraw Funds**: Remove money from user wallets
- **Transfer Funds**: Transfer money between user wallets

### Non-functional Requirements

- **High Availability**: Mission-critical service with minimal downtime
- **Full Traceability**: Complete audit trail of all transactions
- **Security**: Secure handling of financial transactions
- **Scalability**: Designed to handle high transaction volumes

## Technical Stack

- **Framework**: Spring Boot 3.2.3
- **Database**: PostgreSQL 15
- **API**: REST/HTTP
- **Documentation**: OpenAPI/Swagger
- **Container**: Docker & Docker Compose

## Prerequisites

Before running the application, make sure you have the following installed:

- Java 17 or later
- Docker and Docker Compose
- Maven (for local development)

## Getting Started

### Running with Docker Compose (Recommended)

1. Clone the repository:
```bash
git clone https://github.com/yourusername/wallet.git
cd wallet
```

2. Start the application using Docker Compose:
```bash
docker-compose up --build
```

The application will be available at:
- API: http://localhost:8080/api/wallets
- Swagger UI: http://localhost:8080/swagger-ui.html

### Running Locally (Development)

1. Clone the repository:
```bash
git clone https://github.com/yourusername/wallet.git
cd wallet
```

2. Start PostgreSQL database:
```bash
docker-compose up db
```

3. Build and run the application:
```bash
./mvnw spring-boot:run
```

## API Documentation

The API documentation is available through Swagger UI at http://localhost:8080/swagger-ui.html

### Available Endpoints

1. Create Wallet
```http
POST /api/wallets
Content-Type: application/json

{
    "userId": "user123"
}
```

2. Get Balance
```http
GET /api/wallets/{userId}/balance
```

3. Get Historical Balance
```http
GET /api/wallets/{userId}/history?timestamp=2024-03-20T10:00:00
```

4. Deposit Funds
```http
POST /api/wallets/{userId}/deposit
Content-Type: application/json

{
    "amount": 100.00,
    "description": "Initial deposit"
}
```

5. Withdraw Funds
```http
POST /api/wallets/{userId}/withdraw
Content-Type: application/json

{
    "amount": 50.00,
    "description": "Withdrawal"
}
```

6. Transfer Funds
```http
POST /api/wallets/{fromUserId}/transfer/{toUserId}
Content-Type: application/json

{
    "amount": 25.00,
    "description": "Transfer"
}
```

7. Get Transaction History
```http
GET /api/wallets/{userId}/transactions
```

## Development

### Project Structure

```
src/main/java/com/wallet/
├── WalletServiceApplication.java
├── controller/
│   └── WalletController.java
├── model/
│   ├── Wallet.java
│   ├── Transaction.java
│   └── TransactionType.java
├── repository/
│   ├── WalletRepository.java
│   └── TransactionRepository.java
├── service/
│   ├── WalletService.java
│   └── impl/
│       └── WalletServiceImpl.java
└── exception/
    └── WalletException.java
```

### Building the Project

```bash
./mvnw clean package
```

### Running Tests

```bash
./mvnw test
```

## Docker Configuration

The project includes two Docker configurations:

1. `Dockerfile`: Multi-stage build for the application
2. `docker-compose.yml`: Orchestrates the application and PostgreSQL database

### Environment Variables

The following environment variables can be configured:

- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SERVER_PORT`: Application port (default: 8080)

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

[Contact information will be added]