# StockFlow - Portfolio Management Platform

A microservice-based stock portfolio manager built with Java 17, Spring Boot, Angular 17, Kafka, and MySQL. Users can register, build a portfolio of stocks, execute simulated buy/sell trades, and view live (mocked) market prices.

## Architecture

```
Angular UI (4200)
       |
API Gateway (8080) - Spring Cloud Gateway
    /     |     \
user-svc  portfolio-svc  market-svc
 (8081)     (8082)         (8083)
    \        |          /
         Kafka (9092)
    \        |          /
         MySQL (3306)
   users_db  portfolio_db  market_db
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| **api-gateway** | 8080 | Routes requests to backend services, CORS config |
| **user-service** | 8081 | Registration, login, profile (BCrypt, Kafka producer) |
| **portfolio-service** | 8082 | Holdings, buy/sell trades (Kafka consumer & producer) |
| **market-service** | 8083 | Stock prices, price history, 30s scheduled price refresh |
| **stockflow-ui** | 4200 | Angular 17 SPA with Highcharts Dashboards |
| **MySQL** | 3306 | Databases: users_db, portfolio_db, market_db |
| **Kafka** | 9092 | Async messaging between services |

## Environment Requirements

| Tool | Version | Required For |
|------|---------|--------------|
| **Docker** | 20.10+ | Running the full stack |
| **Docker Compose** | v2+ | Orchestrating all containers |
| **Java** | 17+ | Local backend development |
| **Gradle** | 8.7+ | Local backend builds (wrapper included) |
| **Node.js** | 20+ | Local frontend development |
| **npm** | 9+ | Angular dependency management |

> Docker and Docker Compose are the only hard requirements. Everything else is only needed for local development outside Docker.

## Quick Start (Docker)

```bash
# Clone and navigate to the project
cd StockFlow

# Start all services
docker-compose up --build
```

Wait for all services to start (first build takes a few minutes), then open:
- **UI**: http://localhost:4200
- **API Gateway**: http://localhost:8080
- **Actuator health checks**: http://localhost:8081/actuator/health, etc.

To force a full rebuild after code changes (Docker may cache old layers):
```bash
docker-compose build --no-cache
docker-compose up -d
```

To stop:
```bash
docker-compose down
```

To stop and remove data volumes:
```bash
docker-compose down -v
```

## Local Development

### Backend (any single service)

Start infrastructure first:
```bash
docker-compose up mysql zookeeper kafka
```

Then run a service with Gradle:
```bash
cd user-service
../gradlew bootRun
```

Each service's `application.yml` defaults to `localhost` for MySQL and Kafka connections.

### Frontend

```bash
cd stockflow-ui
npm install
npm start
```

Serves at http://localhost:4200 with a proxy forwarding `/users/**`, `/portfolio/**`, `/market/**` to the API Gateway at `localhost:8080`.

## Usage Flow

1. Open http://localhost:4200
2. **Register** a new account (portfolio is auto-created via Kafka)
3. **Dashboard** - view stock price bar chart, holdings pie chart, market overview table
4. **Market** - browse all stocks with live prices (refresh every 30s), view price history line chart
5. **Portfolio** - buy/sell stocks, view holdings column chart and trade history
6. **Profile** - view account details

## Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `user.registered` | user-service | portfolio-service | Auto-create portfolio on signup |
| `portfolio.trade.executed` | portfolio-service | market-service | Log trade activity |
| `market.price.updated` | market-service | portfolio-service | Recalculate portfolio values |

## Database Migrations

Flyway runs automatically on service startup. Migration scripts are located in each service under:
```
src/main/resources/db/migration/
```

The MySQL init script (`mysql-init/init.sql`) creates the three databases on first container start.

## Tech Stack

**Backend**: Java 17, Spring Boot 3.5.11, Spring Cloud Gateway, Spring Data JPA, Spring Kafka, Flyway, Lombok, MySQL 8

**Frontend**: Angular 17, Highcharts, Highcharts Dashboards, TailwindCSS

**Infrastructure**: Docker, Docker Compose, Kafka (Confluent), Zookeeper, Nginx
