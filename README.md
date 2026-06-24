# 🏫 School Teacher Management Service

A production-ready **Spring Boot microservice** for managing teacher records in a school system.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2.4 |
| Language | Java 17 |
| Data | Spring Data JPA + H2 (in-memory) |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Validation | Jakarta Bean Validation |
| Build | Maven |

---

## Quick Start

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

- **Swagger UI:** http://localhost:8080/swagger-ui.html  
- **H2 Console:** http://localhost:8080/h2-console  
- **Health:** http://localhost:8080/actuator/health

---

## API Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/teachers` | Create teacher |
| `GET` | `/api/v1/teachers` | Get all (paginated) |
| `GET` | `/api/v1/teachers/{id}` | Get by ID |
| `GET` | `/api/v1/teachers/email/{email}` | Get by email |
| `GET` | `/api/v1/teachers/department/{dept}` | Get by department |
| `GET` | `/api/v1/teachers/status/{status}` | Get by status |
| `GET` | `/api/v1/teachers/search?keyword=` | Full-text search |
| `PUT` | `/api/v1/teachers/{id}` | Update teacher |
| `PATCH` | `/api/v1/teachers/{id}/status` | Update status only |
| `DELETE` | `/api/v1/teachers/{id}` | Delete teacher |

---

## Project Structure

```
src/main/java/com/school/teacher/
├── controller/     # REST endpoints
├── service/        # Business logic (interface + impl)
├── repository/     # JPA repositories
├── entity/         # JPA entities
├── dto/            # Request / Response / Paged DTOs
├── exception/      # Custom exceptions + GlobalExceptionHandler
└── config/         # OpenAPI, DataInitializer
```

---

## KT-Agent

An automated Knowledge Transfer Document Generator is included under `KT-Agent/`.

```bash
# Generate a KT document for this project
node KT-Agent/scripts/generate-kt.js
```

See [KT-Agent/README.md](KT-Agent/README.md) for full setup instructions including:
- VSCode + GitHub Copilot integration
- VSCode + Claude integration  
- GitHub Actions automatic trigger
- Reviewer notification setup
