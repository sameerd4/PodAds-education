#!/bin/bash

# Continuous Load Test Script
# Generates continuous load to test observability dashboards in real-time
# Usage: ./scripts/load-test-continuous.sh [interval]
#   interval: Seconds between requests (default: 2)
# 
# Press Ctrl+C to stop

set -e

INTERVAL=${1:-2}
BASE_URL="http://localhost:8000"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔄 Continuous Load Test"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Generating continuous load (1 request every $INTERVAL seconds)"
echo "Press Ctrl+C to stop"
echo ""

# Check if backend is running
if ! curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo "❌ Backend is not running on port 8000"
    echo "   Start it with: cd apps/api-java && mvn spring-boot:run"
    exit 1
fi

echo "✅ Backend is running"
echo "✅ Starting load test..."
echo ""
echo "📊 Monitor dashboards at: http://localhost:3001"
echo ""

CATEGORIES=("fitness" "tech" "sports" "finance" "comedy" "news" "education" "true-crime")
SLOT_TYPES=("pre-roll" "mid-roll" "post-roll")
TIERS=("free" "premium")
DEVICES=("mobile" "desktop" "tablet")
GEO_LOCATIONS=("US" "CA" "UK" "DE" "FR")

REQUEST_COUNT=0
SUCCESS_COUNT=0
FAIL_COUNT=0

# Trap Ctrl+C to show summary
trap 'echo ""; echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"; echo "📊 Load Test Summary"; echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"; echo "  Total Requests: $REQUEST_COUNT"; echo "  Successful: $SUCCESS_COUNT"; echo "  Failed: $FAIL_COUNT"; echo ""; exit 0' INT

while true; do
    # Randomize request parameters for realistic load
    CATEGORY=${CATEGORIES[$((RANDOM % ${#CATEGORIES[@]}))]}
    SLOT_TYPE=${SLOT_TYPES[$((RANDOM % ${#SLOT_TYPES[@]}))]}
    TIER=${TIERS[$((RANDOM % ${#TIERS[@]}))]}
    DEVICE=${DEVICES[$((RANDOM % ${#DEVICES[@]}))]}
    GEO=${GEO_LOCATIONS[$((RANDOM % ${#GEO_LOCATIONS[@]}))]}
    SEED=$((RANDOM % 100000))
    TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    
    ((REQUEST_COUNT++))
    
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/decision?seed=$SEED" \
      -H 'Content-Type: application/json' \
      -d "{
        \"requestId\":\"load-test-$(date +%s)-$REQUEST_COUNT\",
        \"podcast\":{\"category\":\"$CATEGORY\",\"show\":\"load-test-show\",\"episode\":\"ep-$REQUEST_COUNT\"},
        \"slot\":{\"type\":\"$SLOT_TYPE\"},
        \"listener\":{\"geo\":\"$GEO\",\"device\":\"$DEVICE\",\"tier\":\"$TIER\",\"consent\":true,\"timeOfDay\":\"afternoon\"},
        \"timestamp\":\"$TIMESTAMP\"
      }" 2>&1)
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -1)
    
    if [ "$HTTP_CODE" = "200" ]; then
        ((SUCCESS_COUNT++))
        STATUS="✅"
    else
        ((FAIL_COUNT++))
        STATUS="❌"
    fi
    
    # Show status every request (can be modified to show less frequently)
    echo "[$(date +%H:%M:%S)] $STATUS Request #$REQUEST_COUNT | $CATEGORY | $SLOT_TYPE | HTTP $HTTP_CODE"
    
    sleep $INTERVAL
done
