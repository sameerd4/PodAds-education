# PodAds Lab API (Java/Spring Boot)

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Building

```bash
mvn clean install
```

## Running

```bash
mvn spring-boot:run
```

Or run the JAR:

```bash
java -jar target/podads-api-1.0.0.jar
```

The API will be available at `http://localhost:8000`

## Testing

```bash
curl -X POST "http://localhost:8000/v1/decision?seed=12345" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "req-test-123",
    "podcast": {
      "category": "tech",
      "show": "The Daily Run",
      "episode": "Episode 42"
    },
    "slot": {
      "type": "mid-roll",
      "cuePoint": 300
    },
    "listener": {
      "geo": "US",
      "device": "mobile",
      "tier": "premium",
      "consent": true,
      "timeOfDay": "afternoon"
    },
    "timestamp": "2024-01-15T10:30:00Z"
  }'
```

## Architecture

Clean Architecture with:
- `domain/` - Entities and value objects
- `application/` - Use cases
- `infrastructure/` - Filters, sourcing, external adapters
- `api/` - REST controllers and DTOs


