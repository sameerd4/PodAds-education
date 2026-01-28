# PodAds Lab - Architecture Overview

## Project Structure

**Monorepo** (pnpm workspace) with 3 main applications:

```
PodAds/
├── apps/
│   ├── web/          # React frontend
│   └── api-java/     # Spring Boot backend
├── packages/
│   └── shared/       # Shared TypeScript types
└── infra/            # Observability stack (Docker)
```

---

## Services & Tech Stack

### 1. **Frontend** (`apps/web/`)
**Tech Stack:**
- **Framework:** React 18 + TypeScript
- **Build Tool:** Vite
- **Routing:** React Router v6
- **Styling:** Tailwind CSS
- **Charts:** Recharts
- **State:** Zustand
- **Forms:** React Hook Form + Zod

**Features:**
- Live decision simulator (`/live`)
- Scenario loader (`/scenarios`)
- Dashboard (`/dashboard`) - queries Prometheus via backend proxy
- Mobile responsive

---

### 2. **Backend API** (`apps/api-java/`)
**Tech Stack:**
- **Framework:** Spring Boot 3.2.0
- **Language:** Java 21
- **Build:** Maven
- **Metrics:** Micrometer + Prometheus
- **Logging:** Logback (JSON structured logs)

**Endpoints:**
- `POST /v1/decision` - Ad decision endpoint
- `GET /actuator/health` - Health check
- `GET /actuator/prometheus` - Metrics export
- `GET /api/metrics/query` - Prometheus proxy (instant queries)
- `GET /api/metrics/query_range` - Prometheus proxy (range queries)

**Data:**
- No database - uses JSON fixtures from `src/main/resources/fixtures/`
- In-memory state (counters, timers)

---

### 3. **Prometheus** (`infra/prometheus/`)
**Tech Stack:**
- **Tool:** Prometheus (time-series database)
- **Config:** YAML configuration files
- **Storage:** Local Docker volume

**Purpose:**
- Scrapes backend metrics every 15 seconds
- Stores historical metrics (local Docker volume)
- Provides query API for dashboard

**Configuration:**
- Scrapes: `http://localhost:8000/actuator/prometheus` (local development)
- Scrape interval: 15s

---

## Local Deployment

All services run locally using Docker Compose:

### **Infrastructure Stack** (`infra/docker-compose.yml`)
- **Postgres:** Database for application data
- **Redis:** Caching and session storage
- **Prometheus:** Metrics collection and storage
- **Grafana:** Metrics visualization and dashboards

All services are configured to run on localhost with standard ports:
- Postgres: `localhost:5432`
- Redis: `localhost:6379`
- Prometheus: `localhost:9090`
- Grafana: `localhost:3001`

### **Application Services**
- **Frontend:** Runs via `pnpm dev:web` on `localhost:5173`
- **Backend:** Runs via `mvn spring-boot:run` in `apps/api-java/` on `localhost:8000`

---

## Data Flow

```
User Browser
    ↓
localhost:5173 (Frontend)
    ↓ API calls
localhost:8000 (Backend API)
    ↓ Metrics export
localhost:9090 (Prometheus)
    ↓ Scrapes every 15s
Backend /actuator/prometheus
    ↓ Dashboard queries
Backend /api/metrics/query (proxy)
    ↓ Proxies to
Prometheus API
    ↓ Visualization
localhost:3001 (Grafana)
```

---

## Environment Variables

### **Frontend**
- `VITE_API_URL` - Backend API URL (default: `http://localhost:8000` for local development)
- `VITE_PROMETHEUS_URL` - (Optional, for direct Prometheus access)

### **Backend**
- `CORS_ALLOWED_ORIGINS` - Comma-separated origins (default: `http://localhost:5173` for local development)
- `PROMETHEUS_URL` - Prometheus service URL (default: `http://localhost:9090` for local development)
- `PORT` - Server port (default: `8000`)
- `SPRING_DATASOURCE_URL` - Postgres connection URL (default: `jdbc:postgresql://localhost:5432/podads`)
- `SPRING_DATA_REDIS_HOST` - Redis host (default: `localhost`)
- `SPRING_DATA_REDIS_PORT` - Redis port (default: `6379`)

### **Prometheus (Docker)**
- Configured via `infra/prometheus/prometheus.yml`
- Scrapes backend at `http://localhost:8000/actuator/prometheus` (local development)

---

## Key Features

1. **No Database** - Backend uses JSON fixtures, completely stateless
2. **Metrics Persistence** - Prometheus stores historical data
3. **Backend Proxy** - Frontend queries Prometheus through backend (avoids CORS)
4. **Mobile Responsive** - All pages optimized for mobile
5. **Real-time Dashboard** - Auto-refreshes every 10 seconds

---

## Local Development

**Frontend:**
```bash
cd apps/web
pnpm dev  # Runs on http://localhost:5173
```

**Backend:**
```bash
cd apps/api-java
mvn spring-boot:run  # Runs on http://localhost:8000
```

**Prometheus (Docker):**
```bash
cd infra
docker-compose up prometheus  # Runs on http://localhost:9090
```

---

## Summary

- **Services:** Frontend (React), Backend (Spring Boot), Prometheus, Grafana
- **Local Setup:** All services run via Docker Compose and local development servers
- **Monorepo:** pnpm workspace with shared TypeScript types
- **No Database:** Stateless backend with fixtures (Postgres available for future use)
- **Observability:** Prometheus for metrics, Grafana for visualization, structured JSON logs
