#!/bin/bash

# Test Dashboards Script
# Generates test data to verify observability dashboards are working
# Usage: ./scripts/test-dashboards.sh [count]
#   count: Number of requests to send (default: 30)

set -e

COUNT=${1:-30}
BASE_URL="http://localhost:8000"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🧪 Testing Observability Dashboards"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if backend is running
if ! curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo "❌ Backend is not running on port 8000"
    echo "   Start it with: cd apps/api-java && mvn spring-boot:run"
    exit 1
fi

echo "✅ Backend is running"
echo ""

# Check if Prometheus is accessible (optional)
if curl -s http://localhost:9090/api/v1/status/config > /dev/null 2>&1; then
    echo "✅ Prometheus is running"
else
    echo "⚠️  Prometheus not accessible (optional - metrics still collected)"
fi

echo ""
echo "📊 Generating $COUNT test requests..."
echo ""

CATEGORIES=("fitness" "tech" "sports" "finance" "comedy" "news" "education" "true-crime")
SLOT_TYPES=("pre-roll" "mid-roll" "post-roll")
TIERS=("free" "premium")
DEVICES=("mobile" "desktop" "tablet")

SUCCESS=0
FAILED=0

for i in $(seq 1 $COUNT); do
    # Randomize request parameters
    CATEGORY=${CATEGORIES[$((RANDOM % ${#CATEGORIES[@]}))]}
    SLOT_TYPE=${SLOT_TYPES[$((RANDOM % ${#SLOT_TYPES[@]}))]}
    TIER=${TIERS[$((RANDOM % ${#TIERS[@]}))]}
    DEVICE=${DEVICES[$((RANDOM % ${#DEVICES[@]}))]}
    SEED=$((10000 + i + RANDOM))
    
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/v1/decision?seed=$SEED" \
      -H 'Content-Type: application/json' \
      -d "{
        \"requestId\":\"dashboard-test-$i\",
        \"podcast\":{\"category\":\"$CATEGORY\",\"show\":\"test-show-$i\",\"episode\":\"ep-$i\"},
        \"slot\":{\"type\":\"$SLOT_TYPE\"},
        \"listener\":{\"geo\":\"US\",\"device\":\"$DEVICE\",\"tier\":\"$TIER\",\"consent\":true,\"timeOfDay\":\"afternoon\"},
        \"timestamp\":\"2026-01-22T12:00:00Z\"
      }")
    
    if [ "$HTTP_CODE" = "200" ]; then
        ((SUCCESS++))
        if [ $((i % 5)) -eq 0 ]; then
            echo "  ✅ Sent $i/$COUNT requests..."
        fi
    else
        ((FAILED++))
        echo "  ❌ Request $i failed (HTTP $HTTP_CODE)"
    fi
    
    # Small delay to see metrics update
    sleep 0.3
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📈 Results"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  ✅ Successful: $SUCCESS"
if [ $FAILED -gt 0 ]; then
    echo "  ❌ Failed: $FAILED"
fi
echo ""

# Check metrics
echo "📊 Current Metrics:"
echo ""
echo "  Total Requests:"
curl -s "$BASE_URL/actuator/prometheus" | grep "^ad_requests_total" | awk '{print "    " $1 " = " $2}' || echo "    (no data yet)"
echo ""
echo "  Total Decisions:"
curl -s "$BASE_URL/actuator/prometheus" | grep "^ad_decisions_total" | head -3 | awk '{print "    " $1 " = " $2}' || echo "    (no data yet)"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔍 Verification Steps"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Wait 10-15 seconds for Prometheus to scrape metrics"
echo "2. Open Grafana: http://localhost:3001 (admin/admin)"
echo "3. Navigate to Dashboards → Browse"
echo "4. Check these dashboards:"
echo "   - PodAds Lab - Latency Dashboard"
echo "   - PodAds Lab - Execution Metrics Dashboard"
echo "   - PodAds Lab - Error Metrics Dashboard"
echo "   - PodAds Lab - Health & Well-being Dashboard"
echo ""
echo "5. Verify metrics are updating:"
echo "   - Request rate should show activity"
echo "   - Fill rate should be > 0%"
echo "   - Latency metrics should show values"
echo "   - Error rate should be low/zero"
echo ""
echo "✅ Test complete! Metrics should be visible in dashboards within 15 seconds."
echo ""
