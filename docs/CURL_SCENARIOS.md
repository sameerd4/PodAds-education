# Curl Test Scenarios for PodAds Lab

Collection of curl commands to test different ad serving scenarios.

**Base URL:** `http://localhost:8000/v1/decision`

---

## Scenario 1: Fitness Category - Free Tier User (US Mobile)

**Expected:** Nike or Adidas campaigns should win (fitness category, free tier eligible)

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=12345' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-1-fitness-free",
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
  }'
```

---

## Scenario 2: Tech Category - Premium Tier User (US Desktop)

**Expected:** Apple or Samsung campaigns should win (tech category, premium tier)

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=54321' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-2-tech-premium",
    "podcast": {
      "category": "tech",
      "show": "Tech Talk Weekly",
      "episode": "ep-042"
    },
    "slot": {
      "type": "pre-roll"
    },
    "listener": {
      "geo": "US",
      "device": "desktop",
      "tier": "premium",
      "consent": true,
      "timeOfDay": "morning"
    },
    "timestamp": "2026-01-22T08:00:00Z"
  }'
```

---

## Scenario 3: Finance Category - Premium User (CA Smart Speaker)

**Expected:** Chase or American Express campaigns should win (finance category, premium tier)

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=99999' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-3-finance-premium",
    "podcast": {
      "category": "finance",
      "show": "Money Matters",
      "episode": "ep-100"
    },
    "slot": {
      "type": "post-roll"
    },
    "listener": {
      "geo": "CA",
      "device": "smart-speaker",
      "tier": "premium",
      "consent": true,
      "timeOfDay": "evening"
    },
    "timestamp": "2026-01-22T20:00:00Z"
  }'
```

---

## Scenario 4: True Crime Category - Free Tier (GB Mobile)

**Expected:** Spotify campaign should win (targets true-crime, free tier)

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=77777' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-4-truecrime-free",
    "podcast": {
      "category": "true-crime",
      "show": "Mystery Hour",
      "episode": "ep-050"
    },
    "slot": {
      "type": "mid-roll",
      "cuePoint": 600
    },
    "listener": {
      "geo": "GB",
      "device": "mobile",
      "tier": "free",
      "consent": true,
      "timeOfDay": "night"
    },
    "timestamp": "2026-01-22T22:00:00Z"
  }'
```

---

## Scenario 5: News Category - Premium User (US Desktop)

**Expected:** Tesla campaign should win (targets tech + news, premium tier)

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=11111' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-5-news-premium",
    "podcast": {
      "category": "news",
      "show": "Daily Brief",
      "episode": "ep-200"
    },
    "slot": {
      "type": "pre-roll"
    },
    "listener": {
      "geo": "US",
      "device": "desktop",
      "tier": "premium",
      "consent": true,
      "timeOfDay": "morning"
    },
    "timestamp": "2026-01-22T07:00:00Z"
  }'
```

---

## Scenario 6: Comedy Category - Free Tier (US Mobile)

**Expected:** Spotify campaign should win (targets comedy, free tier, excludes news)

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=22222' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-6-comedy-free",
    "podcast": {
      "category": "comedy",
      "show": "Laugh Track",
      "episode": "ep-075"
    },
    "slot": {
      "type": "mid-roll",
      "cuePoint": 450
    },
    "listener": {
      "geo": "US",
      "device": "mobile",
      "tier": "free",
      "consent": true,
      "timeOfDay": "afternoon"
    },
    "timestamp": "2026-01-22T15:00:00Z"
  }'
```

---

## Scenario 7: Premium Tier Only Campaign Test (Free User Should Fail)

**Expected:** Adidas campaign should be filtered out (premium tier only, user is free tier)

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=33333' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-7-premium-only-filter",
    "podcast": {
      "category": "fitness",
      "show": "Workout Wednesday",
      "episode": "ep-030"
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
    "timestamp": "2026-01-22T14:00:00Z"
  }'
```

**Note:** Check filter results - Adidas (camp-002) should fail `TierTargetingFilter`

---

## Scenario 8: High Bid Campaign Test (Tech Premium)

**Expected:** Apple campaign should win (highest bid: $12.00 CPM)

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=44444' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-8-high-bid",
    "podcast": {
      "category": "tech",
      "show": "Innovation Hour",
      "episode": "ep-150"
    },
    "slot": {
      "type": "pre-roll"
    },
    "listener": {
      "geo": "US",
      "device": "desktop",
      "tier": "premium",
      "consent": true,
      "timeOfDay": "morning"
    },
    "timestamp": "2026-01-22T09:00:00Z"
  }'
```

**Expected Winner:** Apple iPhone 15 Pro (camp-003) with $12.00 CPM bid

---

## Scenario 9: Car Device Test (Spotify Eligible)

**Expected:** Spotify campaign should win (targets car device)

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=55555' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-9-car-device",
    "podcast": {
      "category": "comedy",
      "show": "Road Trip Radio",
      "episode": "ep-088"
    },
    "slot": {
      "type": "mid-roll",
      "cuePoint": 500
    },
    "listener": {
      "geo": "US",
      "device": "car",
      "tier": "free",
      "consent": true,
      "timeOfDay": "afternoon"
    },
    "timestamp": "2026-01-22T16:00:00Z"
  }'
```

---

## Scenario 10: Non-US Geo Test (Should Filter Some Campaigns)

**Expected:** Only campaigns targeting CA/GB/AU should pass geo filter

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=66666' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-10-non-us-geo",
    "podcast": {
      "category": "tech",
      "show": "Global Tech",
      "episode": "ep-200"
    },
    "slot": {
      "type": "post-roll"
    },
    "listener": {
      "geo": "AU",
      "device": "mobile",
      "tier": "premium",
      "consent": true,
      "timeOfDay": "evening"
    },
    "timestamp": "2026-01-22T19:00:00Z"
  }'
```

**Note:** Check filter results - campaigns only targeting US should fail `GeoTargetingFilter`

---

## Scenario 11: News Category (Should Exclude Spotify)

**Expected:** Tesla should win (Spotify excludes news category)

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=88888' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-11-news-exclusion",
    "podcast": {
      "category": "news",
      "show": "Evening News",
      "episode": "ep-300"
    },
    "slot": {
      "type": "pre-roll"
    },
    "listener": {
      "geo": "US",
      "device": "mobile",
      "tier": "free",
      "consent": true,
      "timeOfDay": "evening"
    },
    "timestamp": "2026-01-22T18:00:00Z"
  }'
```

**Note:** Spotify (camp-007) should fail `ExcludedCategoryFilter` for news

---

## Scenario 12: Multiple Eligible Campaigns (Auction Test)

**Expected:** Highest scoring campaign wins based on bid × match × pacing

```bash
curl -X POST 'http://localhost:8000/v1/decision?seed=12121' \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "scenario-12-auction-test",
    "podcast": {
      "category": "fitness",
      "show": "Fitness First",
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
    "timestamp": "2026-01-22T13:00:00Z"
  }'
```

**Expected:** Multiple Nike campaigns should be eligible, winner determined by auction score

---

## Quick Test Script

Save all scenarios to a file and run them:

```bash
# Save scenarios to test-scenarios.sh
chmod +x test-scenarios.sh
./test-scenarios.sh
```

Or test individually:

```bash
# Test scenario 1
curl -X POST 'http://localhost:8000/v1/decision?seed=12345' \
  -H "Content-Type: application/json" \
  -d '{"requestId":"test-1","podcast":{"category":"fitness","show":"Test","episode":"ep-1"},"slot":{"type":"mid-roll","cuePoint":300},"listener":{"geo":"US","device":"mobile","tier":"free","consent":true,"timeOfDay":"afternoon"},"timestamp":"2026-01-22T12:00:00Z"}' | jq '.winner.serve.brandName'
```

---

## Expected Results Summary

| Scenario | Category | Tier | Expected Winner | Why |
|----------|----------|------|----------------|-----|
| 1 | Fitness | Free | Nike | Fitness category, free tier eligible |
| 2 | Tech | Premium | Apple | Tech category, premium tier, highest bid ($12.00) |
| 3 | Finance | Premium | Chase/Amex | Finance category, premium tier |
| 4 | True Crime | Free | Spotify | True-crime category, free tier |
| 5 | News | Premium | Tesla | News + tech categories, premium tier |
| 6 | Comedy | Free | Spotify | Comedy category, free tier |
| 7 | Fitness | Free | Nike | Adidas filtered (premium only) |
| 8 | Tech | Premium | Apple | Highest bid ($12.00 CPM) |
| 9 | Comedy | Free | Spotify | Car device targeting |
| 10 | Tech | Premium | Apple | Geo targeting (AU eligible) |
| 11 | News | Free | Tesla | Spotify excluded (news category) |
| 12 | Fitness | Free | Nike | Auction winner (highest score) |

---

## Tips

1. **Use `jq` for pretty output:**
   ```bash
   curl ... | jq '.winner.serve.brandName'
   ```

2. **Check filter results:**
   ```bash
   curl ... | jq '.candidates[] | {campaignId, brandName, passedAllFilters, filterResults}'
   ```

3. **See all candidates:**
   ```bash
   curl ... | jq '.candidates[] | {brandName, finalScore: .score.finalScore, passedAllFilters}'
   ```

4. **Check pipeline stages:**
   ```bash
   curl ... | jq '.stages[] | {stageName, outputSummary, latencyMs}'
   ```
