# API Documentation

## Base URL

- Development: `http://localhost:8000`
- Production: TBD

## Authentication

Currently no authentication required (demo only).

## Endpoints

### POST /v1/decision

Make an ad decision.

**Request Body:**
```json
{
  "requestId": "req-123",
  "podcast": {
    "category": "fitness",
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
    "tier": "free",
    "consent": true,
    "timeOfDay": "afternoon"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Response:**
```json
{
  "decisionId": "dec-1234567890-12345",
  "requestId": "req-123",
  "seed": 12345,
  "timestamp": "2024-01-15T10:30:00Z",
  "stages": [...],
  "candidates": [...],
  "winner": {
    "candidate": {...},
    "serve": {...}
  }
}
```

### POST /v1/events

Record a delivery event (impression, quartile, complete, click).

**Request Body:**
```json
{
  "decisionId": "dec-1234567890-12345",
  "eventType": "impression",
  "timestamp": "2024-01-15T10:30:00Z",
  "metadata": {}
}
```

**Response:**
```json
{
  "status": "recorded"
}
```

### GET /v1/trace/{decisionId}

Get a decision trace by ID for replay.

**Response:**
```json
{
  "decisionId": "dec-1234567890-12345",
  "request": {...},
  "decision": {...},
  "createdAt": "2024-01-15T10:30:00Z"
}
```

## OpenAPI Spec

Full OpenAPI specification available at `/openapi.json` (when implemented).

See [openapi.yaml](../openapi.yaml) for complete schema definitions.


