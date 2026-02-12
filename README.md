# AutoDepot

Dispatcher dashboard for managing orders, trips, drivers, and vehicles. Supports order creation, trip assignment, breakdown handling, and repair workflows.

## Architecture

```
┌─────────────┐     ┌─────────┐     ┌──────────────┐     ┌────────────┐
│   nginx     │────▶│ frontend │────▶│   backend    │────▶│ PostgreSQL │
│   (port 80) │     │   (Vite) │     │ (Spring Boot)│     │    :5432   │
└─────────────┘     └─────────┘     └──────────────┘     └────────────┘
```

### Backend (Spring Boot)

Layered structure:

- **Controllers** - HTTP handlers, work only with DTOs, delegate to services
- **Services** - business logic, convert entities ↔ DTOs via mappers
- **Data services** - CRUD over entities (OrderService, DriverService, TripDataService, etc.)
- **Repositories** - JDBC access to PostgreSQL

Main flows:

- **Orders** - create, generate random; QUEUED until trip assigned
- **Trips** - assign (order → driver + car), complete, breakdown, request repair, confirm repair
- **Drivers** - create, edit (name, license categories, license year)
- **Dashboard** - stats, pending orders, active trips, driver performance, activity log

### Frontend (React + Vite)

- SPA with React 18, TypeScript
- TanStack Query for data fetching
- Axios with 401 → redirect to login
- SweetAlert2 for dialogs
- i18n: Ukrainian, English

### Infrastructure

- **Docker Compose** - postgres, backend, frontend, nginx
- **nginx** - reverse proxy, /api and /login → backend
- **Flyway** - DB migrations

## Technologies

| Layer    | Technology |
|----------|------------|
| Backend  | Java 19, Spring Boot 4.0 |
| Web      | Spring MVC, Thymeleaf (server-side) |
| Security | Spring Security, form login |
| DB       | PostgreSQL 16, JDBC |
| ORM      | Spring JDBC (no JPA) |
| Migrations | Flyway |
| Mapping | MapStruct |
| Frontend | React 18, TypeScript, Vite |
| Data     | TanStack Query, Axios |
| Tests    | JUnit 5, Mockito, Testcontainers |

## Run

```bash
docker compose up -d
```

- App: http://localhost
- API: http://localhost/api
- Backend: http://localhost:8080

## Development

```bash
# Backend
./mvnw spring-boot:run

# Frontend
cd frontend && npm run dev

# Tests
./mvnw test
```

## Project structure

```
autoDepot/
├── frontend/          # React SPA
├── nginx/             # nginx config
├── src/main/java/     # Backend
│   └── com/example/autodepot/
│       ├── config/    # DataInitializer, Security, Jackson
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── exception/
│       ├── mapper/
│       ├── repository/
│       ├── security/
│       └── service/
├── src/main/resources/
│   ├── db/migration/  # Flyway
│   └── templates/     # Thymeleaf (dashboard.html)
├── compose.yaml
└── pom.xml
```
