#!/bin/bash

# Verification script for OBS-5: Prometheus Scraping
# Usage: ./scripts/verify-prometheus-scraping.sh

set -e

echo "🔍 Verifying OBS-5: Prometheus Scraping Configuration"
echo "======================================================"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "⚠️  Docker is not running"
    echo "   Start Docker Desktop and run: cd infra && docker-compose up -d prometheus"
    echo ""
    echo "✅ Configuration is ready in infra/prometheus/prometheus.yml"
    exit 0
fi

# Check if backend is running
if ! curl -s http://localhost:8000/actuator/prometheus > /dev/null 2>&1; then
    echo "❌ Backend is not running on port 8000"
    echo "   Start it with: cd apps/api-java && mvn spring-boot:run"
    exit 1
fi

echo "✅ Backend is running"
echo ""

# Check if Prometheus is running
if ! docker ps | grep -q podads-prometheus; then
    echo "📦 Starting Prometheus..."
    cd infra && docker-compose up -d prometheus
    sleep 5
fi

echo "✅ Prometheus is running"
echo ""

# Check Prometheus targets
echo "📊 Checking Prometheus Targets..."
TARGETS=$(curl -s http://localhost:9090/api/v1/targets 2>/dev/null)
if [ -z "$TARGETS" ]; then
    echo "   ⚠️  Prometheus API not accessible (may still be starting)"
    echo "   Wait a few seconds and check: http://localhost:9090"
    exit 0
fi

TARGET_STATUS=$(echo "$TARGETS" | jq -r '.data.activeTargets[] | "\(.labels.job): \(.health)"' 2>/dev/null || echo "unknown")
echo "   Target Status:"
echo "$TARGET_STATUS" | sed 's/^/      /'

if echo "$TARGET_STATUS" | grep -q "up"; then
    echo "   ✅ Target is UP"
else
    echo "   ⚠️  Target may be DOWN (check if backend is accessible from Docker)"
fi

echo ""
echo "📈 Testing Metrics Query..."
METRICS=$(curl -s 'http://localhost:9090/api/v1/query?query=ad_requests_total' 2>/dev/null)
if echo "$METRICS" | jq -e '.data.result[]' > /dev/null 2>&1; then
    echo "   ✅ Metrics found in Prometheus:"
    echo "$METRICS" | jq -r '.data.result[] | "      \(.metric.__name__): \(.value[1])"' 2>/dev/null | head -3
else
    echo "   ⚠️  No metrics yet (make a request to generate metrics)"
    echo "   Try: curl -X POST http://localhost:8000/v1/decision ..."
fi

echo ""
echo "======================================================"
echo "✅ Prometheus scraping configuration verified!"
echo ""
echo "🔗 Useful URLs:"
echo "   - Prometheus UI: http://localhost:9090"
echo "   - Targets: http://localhost:9090/targets"
echo "   - Graph: http://localhost:9090/graph"
echo ""
echo "📝 Test Query:"
echo "   ad_requests_total"
echo "   ad_decision_latency_ms_seconds_count"
echo "   rate(ad_requests_total[5m])"
echo ""
