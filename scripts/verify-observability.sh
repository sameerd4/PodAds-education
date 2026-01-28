#!/bin/bash

# Verification script for OBS-1 and OBS-2
# Usage: ./scripts/verify-observability.sh

set -e

echo "🔍 Verifying OBS-1: Metrics Dependencies"
echo "=========================================="
echo ""

# Check if backend is running
if ! curl -s http://localhost:8000/actuator/health > /dev/null 2>&1; then
    echo "❌ Backend is not running on port 8000"
    echo "   Start it with: cd apps/api-java && mvn spring-boot:run"
    exit 1
fi

echo "✅ Backend is running"
echo ""

# OBS-1: Verify Actuator Endpoints
echo "📊 OBS-1: Testing Actuator Endpoints"
echo "--------------------------------------"

echo "1. Testing /actuator/health..."
HEALTH=$(curl -s http://localhost:8000/actuator/health)
if echo "$HEALTH" | grep -q '"status":"UP"'; then
    echo "   ✅ Health endpoint working"
    echo "   Response: $(echo $HEALTH | jq -r '.status' 2>/dev/null || echo 'UP')"
else
    echo "   ❌ Health endpoint failed"
    exit 1
fi

echo ""
echo "2. Testing /actuator/prometheus..."
PROM_METRICS=$(curl -s http://localhost:8000/actuator/prometheus | head -5)
if echo "$PROM_METRICS" | grep -q "# HELP"; then
    echo "   ✅ Prometheus endpoint working (returns Prometheus format)"
    echo "   First 3 lines:"
    echo "$PROM_METRICS" | head -3 | sed 's/^/      /'
else
    echo "   ❌ Prometheus endpoint failed"
    exit 1
fi

echo ""
echo "3. Testing /actuator/metrics..."
METRICS_LIST=$(curl -s http://localhost:8000/actuator/metrics | jq -r '.names[]' 2>/dev/null | head -5)
if [ ! -z "$METRICS_LIST" ]; then
    echo "   ✅ Metrics endpoint working"
    echo "   Sample metrics:"
    echo "$METRICS_LIST" | sed 's/^/      - /'
else
    echo "   ❌ Metrics endpoint failed"
    exit 1
fi

echo ""
echo "📈 OBS-2: Testing Custom Metrics"
echo "--------------------------------"

# Make a test request to generate metrics
echo "1. Making test request to generate metrics..."
curl -s -X POST 'http://localhost:8000/v1/decision?seed=12345' \
  -H 'Content-Type: application/json' \
  -d '{
    "requestId":"verify-obs",
    "podcast":{"category":"fitness","show":"test","episode":"test"},
    "slot":{"type":"mid-roll"},
    "listener":{"geo":"US","device":"mobile","tier":"free","consent":true,"timeOfDay":"afternoon"},
    "timestamp":"2026-01-22T12:00:00Z"
  }' > /dev/null

sleep 1

echo ""
echo "2. Verifying latency metrics..."
LATENCY_METRICS=$(curl -s http://localhost:8000/actuator/prometheus | grep -E "^ad_decision_latency_ms|^ad_stage_latency_ms" | head -3)
if [ ! -z "$LATENCY_METRICS" ]; then
    echo "   ✅ Latency metrics found:"
    echo "$LATENCY_METRICS" | sed 's/^/      /'
else
    echo "   ❌ Latency metrics not found"
    exit 1
fi

echo ""
echo "3. Verifying execution metrics..."
EXEC_METRICS=$(curl -s http://localhost:8000/actuator/prometheus | grep -E "^ad_requests_total|^ad_decisions_total|^ad_candidates_processed" | head -5)
if [ ! -z "$EXEC_METRICS" ]; then
    echo "   ✅ Execution metrics found:"
    echo "$EXEC_METRICS" | sed 's/^/      /'
else
    echo "   ❌ Execution metrics not found"
    exit 1
fi

echo ""
echo "4. Verifying filter metrics..."
FILTER_METRICS=$(curl -s http://localhost:8000/actuator/prometheus | grep "^ad_filters_applied" | head -3)
if [ ! -z "$FILTER_METRICS" ]; then
    echo "   ✅ Filter metrics found:"
    echo "$FILTER_METRICS" | sed 's/^/      /'
else
    echo "   ❌ Filter metrics not found"
    exit 1
fi

echo ""
echo "5. Checking metric descriptions..."
METRIC_HELP=$(curl -s http://localhost:8000/actuator/prometheus | grep -E "^# HELP ad_" | head -5)
if [ ! -z "$METRIC_HELP" ]; then
    echo "   ✅ Metric descriptions present:"
    echo "$METRIC_HELP" | sed 's/^/      /'
else
    echo "   ⚠️  Metric descriptions not found (non-critical)"
fi

echo ""
echo "=========================================="
echo "✅ All OBS-1 and OBS-2 verifications passed!"
echo ""
echo "📊 Quick Stats:"
echo "   - Total requests: $(curl -s http://localhost:8000/actuator/prometheus | grep '^ad_requests_total' | awk '{print $2}')"
echo "   - Total decisions: $(curl -s http://localhost:8000/actuator/prometheus | grep '^ad_decisions_total' | awk '{sum+=$2} END {print sum}')"
echo ""
echo "🔗 Useful endpoints:"
echo "   - Health: http://localhost:8000/actuator/health"
echo "   - Metrics: http://localhost:8000/actuator/metrics"
echo "   - Prometheus: http://localhost:8000/actuator/prometheus"
echo ""
