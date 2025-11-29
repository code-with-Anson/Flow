# Flow - Personal Multimodal Digital Memory

> When the heart has a scale of time, it begins to flow.

[简体中文](../README.md) | **English**

![星空下的约定](https://github.com/code-with-Anson/code-with-Anson/raw/main/%E9%9C%9E%E9%9B%A8%E6%A8%B1%20x%20%E5%A4%A9%E5%85%89%E6%99%93-2-25.10.7.png)

## 📚 Background

**Flow** is my graduation project, so there may be many imperfections. It is not recommended to use this project for long-term storage of your personal data. If you have a real need for memory storage, I strongly recommend moving to my other repository:
https://github.com/code-with-Anson/LifeBook

## 📖 Introduction

**Flow** is a **Personal Multimodal Digital Memory**, designed to solve the problem of storing, managing, and intelligently retrieving massive amounts of personal digital information.

In the era of information explosion, we have more and more photos, documents, and notes, but it is often difficult to quickly find the information we need, especially unstructured data that cannot be described by simple keywords. **Flow** utilizes cutting-edge **AI Large Models** and **Vector Retrieval** technologies to empower machines with the ability to "understand" data, building a second brain for you that can understand what you say and what you see.

### Core Values

- **Multimodal Fusion**: Unified management of data in various formats such as images, documents, and text.
- **Semantic Retrieval**: Supports "Search Image by Text" and "Semantic Search", breaking the limitations of traditional keyword search.
- **Privacy & Security**: Private deployment, keeping data completely in your own hands.
- **Automated Pipeline**: Asynchronous data processing pipeline based on RabbitMQ, automatically completing file vectorization and index construction.

---

## 🏗️ Architecture

The system adopts a microservices-based layered architecture design, mainly including the following core modules:

- **User Management Module**: Identity authentication and RBAC permission control based on JWT.
- **File Management Module**: Object storage based on MinIO, supporting large file multipart upload.
- **Data Pipeline**: Asynchronous message-driven architecture based on RabbitMQ, handling time-consuming tasks such as file upload and vectorization.
- **Vectorization Service**: Integrated AI models (Spring AI + Google Gemini / Alibaba Bailian), transforming multimodal data into high-dimensional vectors.
- **Search Service**: Vector search engine based on Elasticsearch 8, providing high-performance hybrid search capabilities.

### Core Process

1.  **File Upload**: User Upload -> MinIO Storage -> MySQL Metadata Record -> Send RabbitMQ Message.
2.  **Data Processing**: Consume RabbitMQ Message -> Call AI Vectorization Service -> Generate Semantic Vector -> Write to Elasticsearch Index.
3.  **Intelligent Retrieval**: User Input -> Query Vectorization -> Elasticsearch KNN Search -> Aggregate Metadata -> Return Results.

---

## 🛠️ Tech Stack

### Backend

- **Language**: Java 21
- **Framework**: Spring Boot 3.4.0, Spring Web
- **ORM**: Mybatis-Plus
- **Message Queue**: RabbitMQ (Spring AMQP)
- **Real-time Communication**: Spring WebSocket
- **Security**: JWT (JSON Web Token)
- **AI**: Spring AI

### Frontend

- **Language**: TypeScript
- **Framework**: Vue 3
- **Build Tool**: Vite
- **State Management**: Pinia
- **UI Component Library**: Element Plus
- **Network Request**: Axios

### Data & Middleware

- **Database**: MySQL 8
- **Object Storage**: MinIO
- **Message Queue**: RabbitMQ (Management Plugin)
- **Search Engine**: Elasticsearch 8 + Kibana

### DevOps

- **Containerization**: Docker, Docker Compose
- **System**: Ubuntu 24
- **Reverse Proxy**: Nginx

---

## 🚀 Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 21+
- Node.js 18+ & pnpm

### 1. Start Infrastructure

Use Docker Compose to start middleware such as MySQL, MinIO, RabbitMQ, Elasticsearch with one click.

```bash
cd deploy
docker-compose up -d
```

### 2. Start Backend (Flow Backend)

```bash
cd flow-backend
# Compile
mvn clean install
# Run
mvn spring-boot:run
```

### 3. Start Frontend (Flow Frontend)

```bash
cd flow-frontend
# Install dependencies
pnpm install
# Start dev server
pnpm dev
```

---

## 📂 Project Structure

```
Flow/
├── deploy/              # Docker Compose deployment configuration files
├── flow-backend/        # Backend project (Spring Boot)
│   ├── src/main/java    # Java source code
│   └── src/main/resources # Configuration files
├── flow-frontend/       # Frontend project (Vue 3)
├── 项目蓝图/             # Project design documents and materials
└── README.md            # Project documentation
```

---

## 📝 License

MIT License

---

Copyright © 2025 Anson & Kasumiame Sakura & Tenkou Akatsuki. All Rights Reserved.
