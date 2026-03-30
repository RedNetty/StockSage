# StockSage

## Modern Inventory Management Solution

StockSage is a robust, enterprise-grade inventory management system built with Java and Spring Boot. It provides a comprehensive solution for tracking inventory across multiple warehouses, managing suppliers, and recording detailed transaction history — all backed by a clean RESTful API with role-based access control.

## Key Features

- **Comprehensive Product Management** — Create, update, and track products with detailed attributes including SKU, pricing, and category assignment
- **Category Hierarchy** — Organize products with a flexible multi-level category system
- **Multi-Warehouse Support** — Track inventory levels across multiple physical locations
- **Transaction Logging** — Detailed audit trail of all inventory movements (inbound, outbound, transfers)
- **Supplier Management** — Maintain supplier information and link products to their sources
- **User Authentication & Authorization** — JWT-based auth with role-based access control (ADMIN, MANAGER, USER)
- **RESTful API** — Full API coverage with OpenAPI/Swagger documentation
- **Report Generation** — Export inventory data as CSV, Excel, PDF, or JSON
- **Email Notifications** — Configurable low-stock and reorder alerts
- **Docker Support** — Ready-to-run Docker Compose setup for local development

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 2.7.x**
- **Spring Security** (JWT authentication)
- **Spring Data JPA** / **Hibernate**
- **Flyway** (database migrations)

### Database
- **PostgreSQL 14**

### Infrastructure
- **Docker / Docker Compose**
- **Maven**

### Docs
- **OpenAPI 3 / Swagger UI**

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (or Docker)

### Option 1: Docker Compose (recommended)

```bash
# Copy and configure environment variables
cp .env.example .env
# Edit .env with your desired DB password and JWT secret

# Start the stack
docker-compose up -d
```

The app will be available at `http://localhost:8080`.

### Option 2: Local Development

1. **Create the database:**
   ```sql
   CREATE DATABASE stocksage;
   ```

2. **Configure environment variables** (or set them in `application-local.properties`):
   ```
   DB_URL=jdbc:postgresql://localhost:5432/stocksage
   DB_USERNAME=postgres
   DB_PASSWORD=your_password
   JWT_SECRET=your-secret-key
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

### API Documentation

Once running, visit:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/api-docs`

## Project Structure

```
src/main/java/com/portfolio/stocksage/
├── config/          # Spring configuration (security, cache, async, etc.)
├── controller/      # REST API and web controllers
├── dto/             # Request/response DTOs and mappers
├── entity/          # JPA entities
├── exception/       # Custom exceptions and global handler
├── export/          # Export strategy implementations (CSV, Excel, PDF, JSON)
├── report/          # Report generation logic
├── repository/      # Spring Data JPA repositories
├── scheduler/       # Scheduled tasks (low-stock checks, cleanup)
├── security/        # JWT provider, filters, and security utilities
├── service/         # Business logic interfaces and implementations
├── util/            # Shared utilities
└── validation/      # Custom Bean Validation constraints
```

## Configuration

All sensitive values are externalized via environment variables. See `.env.example` for the full list. Key settings:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC connection URL | `jdbc:postgresql://localhost:5432/stocksage` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | *(required)* |
| `JWT_SECRET` | JWT signing key — use a long random string | *(required in prod)* |
| `JWT_EXPIRATION` | Token validity in milliseconds | `86400000` (24h) |
| `EMAIL_ENABLED` | Enable email notifications | `false` |

## License

See [LICENSE.md](LICENSE.md).
