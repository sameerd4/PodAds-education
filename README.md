# PodAds Lab

An inspectable podcast ad server simulator: Request → Filters → Auction → Serve → Metrics.
## The 5-Step Pipeline

This simulator walks through how podcast ads actually get served:

1. **Request**: Podcast context (show, episode, slot type) + listener context (geo, device, tier)
2. **Filters**: 8-12 filters eliminate ineligible candidates (campaign status, schedule, geo, device, duration, frequency cap, budget, pacing, brand safety)
3. **Auction**: Score = bidCPM × matchScore × pacingMultiplier. Highest score wins.
4. **Serve**: Winner's creative is served with tracking URLs
5. **Metrics**: Fill rate, latency, filter drop-offs, event stream

Every decision is explainable. Every run is deterministic. Same seed, same request, same outcome. You can replay traces and see exactly why an ad won or lost.

## Quick Start

### Prerequisites
- Node.js 18+
- pnpm 8+
- Java 21 (OpenJDK or Oracle JDK)
- Maven 3.6+
- Docker & Docker Compose

### Run Locally

1. **Install dependencies:**
   ```bash
   pnpm install
   ```

2. **Start infrastructure (Postgres, Redis, Prometheus, Grafana):**
   ```bash
   cd infra
   docker-compose up -d
   ```

3. **Start backend:**
   ```bash
   cd apps/api-java
   mvn spring-boot:run
   ```
   The backend will start on http://localhost:8000
   
   **Note:** Keep this terminal open! The backend runs in the foreground.
   
   **First time setup:** Maven will download dependencies on first run. The backend uses Lombok 1.18.38 which is compatible with Java 21. If you see compilation errors, verify your Java version with `java -version` (should be 21.x.x).

4. **Start frontend:**
   ```bash
   pnpm dev:web
   ```
   The frontend will start on http://localhost:5173

5. **Access services:**
   - Frontend: http://localhost:5173
   - API: http://localhost:8000
   - Grafana: http://localhost:3001 (default credentials: admin/admin)
   - Prometheus: http://localhost:9090

### Demo Walkthrough

1. Navigate to `/live` (main demo page)
2. Select a podcast category (Fitness, Tech, Finance, etc.)
3. Configure request parameters (slot type, listener context)
4. Click **"Run once"** to see a decision
5. Explore the pipeline stages (expandable cards)
6. View the Auction Board (candidates, filter status, scores)
7. Click **"Explain winner"** to see why it won
8. Try **"Run 100x"** for batch simulation with charts

## Project Structure

```
PodAds/
├── apps/
│   ├── web/          # React frontend (TypeScript)
│   └── api-java/     # Spring Boot backend (Java 21)
├── packages/
│   └── shared/       # Shared TypeScript types
├── infra/            # Docker compose, Prometheus, Grafana
└── docs/             # Documentation
```

## Documentation

- **[Observability Guide](docs/OBSERVABILITY.md)** - Complete guide to monitoring, alerting, and troubleshooting
- [Architecture Overview](docs/ARCHITECTURE_OVERVIEW.md) - System architecture and design decisions
- [API Documentation](docs/api.md)
- [Demo Script](docs/demo-script.md)
- [Curl Test Scenarios](docs/CURL_SCENARIOS.md) - 12 test scenarios to verify backend functionality

## Testing

```bash
# Frontend tests
pnpm --filter web test

# Backend tests
cd apps/api-java && mvn test

# E2E tests
pnpm --filter web test:e2e
```

### Testing the Backend API

You can test the backend directly with curl. See [CURL_SCENARIOS.md](docs/CURL_SCENARIOS.md) for a full set of test scenarios.

Quick test:
```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=12345' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test-001",
    "podcast": {
      "category": "fitness",
      "show": "The Daily Run",
      "episode": "ep-001"
    },
    "slot": {
      "type": "mid-roll",
      "cuePoint": 300
    },
    "listener": {
      "geo": "US",
      "device": "mobile",
      "tier": "free",
      "consent": true,
      "timeOfDay": "afternoon"
    },
    "timestamp": "2026-01-22T12:00:00Z"
  }' | jq '.winner.serve.brandName, .stages[].latencyMs'
```

**What to expect:**
- Decision ID and winner brand name
- All 5 pipeline stages with latency measurements
- Filter results showing which candidates passed or failed
- Auction scores for eligible candidates
- Total latency typically under 25ms

The backend has been tested with 12 different scenarios covering various categories, tiers, geos, and devices. It handles filters correctly, calculates auction scores, and exports metrics to Prometheus. Everything works as expected.

## Disclaimer

**Demo only. This project uses synthetic data and is not affiliated with any brand or company.**

## License

MIT
