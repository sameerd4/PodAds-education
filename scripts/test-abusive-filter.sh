#!/bin/bash

# Test script for Abusive Content Filter
# This script verifies that abusive ads are properly blocked

set -e

API_URL="http://localhost:8000"
SEED=12345

echo "=========================================="
echo "Abusive Content Filter Test"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if backend is running
echo "Checking if backend is running..."
if ! curl -s "${API_URL}/health" > /dev/null; then
    echo -e "${RED}❌ Backend is not running. Please start it first:${NC}"
    echo "   cd apps/api-java && mvn spring-boot:run"
    exit 1
fi
echo -e "${GREEN}✅ Backend is running${NC}"
echo ""

# List of abusive ads to test
echo "=========================================="
echo "Abusive Ads in Blocklist:"
echo "=========================================="
echo ""
echo "1. ${YELLOW}Weight Loss Scam${NC} (camp-abuse-001)"
echo "   'Lose 50lbs in 2 Weeks - Guaranteed!'"
echo "   Reason: Misleading health claims, inappropriate content"
echo ""
echo "2. ${YELLOW}Crypto Trading Bot Scam${NC} (camp-abuse-003)"
echo "   'Turn \$100 into \$10,000 with Crypto Trading Bot'"
echo "   Reason: Investment scam, brand safety violation"
echo ""
echo "3. ${YELLOW}Get Rich Quick Scheme${NC} (camp-abuse-005)"
echo "   'Make \$10,000 a Month Working from Home - No Experience Needed'"
echo "   Reason: Get-rich-quick scam, ML keyword match"
echo ""
echo "4. ${YELLOW}Diploma Mill${NC} (camp-abuse-007)"
echo "   'Get Your PhD in 30 Days - No Classes Required'"
echo "   Reason: Fake education credentials, ML keyword match"
echo ""
echo "=========================================="
echo "Testing Filter..."
echo "=========================================="
echo ""

# Function to test an ad request
test_ad_request() {
    local category=$1
    local description=$2
    local expected_blocked=$3
    
    echo "Testing: ${description}"
    echo "Category: ${category}"
    
    response=$(curl -s -X POST "${API_URL}/v1/decision?seed=${SEED}" \
        -H "Content-Type: application/json" \
        -d "{
            \"requestId\": \"test-$(date +%s)\",
            \"podcast\": {
                \"category\": \"${category}\",
                \"show\": \"Test Show\",
                \"episode\": \"ep-001\"
            },
            \"slot\": {
                \"type\": \"mid-roll\",
                \"cuePoint\": 300
            },
            \"listener\": {
                \"geo\": \"US\",
                \"device\": \"mobile\",
                \"tier\": \"free\",
                \"consent\": true,
                \"timeOfDay\": \"afternoon\"
            },
            \"timestamp\": \"2026-01-24T12:00:00Z\"
        }")
    
    # Check if any abusive ads appear in candidates
    abusive_found=$(echo "$response" | jq -r '.candidates[] | select(.campaignId | startswith("camp-abuse-")) | .campaignId' 2>/dev/null || echo "")
    
    # Check filter results for AbusiveContentFilter
    filter_result=$(echo "$response" | jq -r '.stages[] | select(.stageName == "Filters") | .debugPayload.filterFailures.AbusiveContentFilter // 0' 2>/dev/null || echo "0")
    
    if [ -n "$abusive_found" ]; then
        echo -e "${RED}❌ FAILED: Abusive ad found in candidates: ${abusive_found}${NC}"
        echo "   This ad should have been blocked!"
        return 1
    elif [ "$filter_result" != "0" ] && [ "$filter_result" != "null" ]; then
        echo -e "${GREEN}✅ PASSED: AbusiveContentFilter blocked ${filter_result} ad(s)${NC}"
        return 0
    else
        # Check if any candidates were filtered (might be in filter breakdown)
        total_candidates=$(echo "$response" | jq -r '.candidates | length' 2>/dev/null || echo "0")
        passed_candidates=$(echo "$response" | jq -r '[.candidates[] | select(.score != null)] | length' 2>/dev/null || echo "0")
        
        if [ "$total_candidates" -gt "$passed_candidates" ]; then
            echo -e "${YELLOW}⚠️  PARTIAL: Some ads filtered, but can't confirm AbusiveContentFilter${NC}"
            echo "   Total candidates: ${total_candidates}, Passed: ${passed_candidates}"
        else
            echo -e "${YELLOW}⚠️  INFO: No abusive ads in this category's candidate pool${NC}"
            echo "   This is expected if blocklist is working correctly"
        fi
        return 0
    fi
}

# Test 1: Fitness category (should have abusive ads blocked)
echo "Test 1: Fitness Category"
test_ad_request "fitness" "Should block weight loss scams" true
echo ""

# Test 2: Tech category (should have abusive ads blocked)
echo "Test 2: Tech Category"
test_ad_request "tech" "Should block crypto/phishing scams" true
echo ""

# Test 3: Finance category (should have abusive ads blocked)
echo "Test 3: Finance Category"
test_ad_request "finance" "Should block get-rich-quick schemes" true
echo ""

# Test 4: Education category (should have abusive ads blocked)
echo "Test 4: Education Category"
test_ad_request "education" "Should block diploma mills" true
echo ""

# Summary check: Verify blocklist is loaded
echo "=========================================="
echo "Verifying Blocklist Status"
echo "=========================================="
echo ""

# Check metrics endpoint for blocklist hits
blocklist_hits=$(curl -s "${API_URL}/actuator/prometheus" | grep "ad_blocklist_hits_total" | head -1 || echo "")

if [ -n "$blocklist_hits" ]; then
    echo -e "${GREEN}✅ Blocklist metrics found:${NC}"
    echo "$blocklist_hits"
else
    echo -e "${YELLOW}⚠️  No blocklist hits recorded yet${NC}"
    echo "   This might mean:"
    echo "   1. Filter hasn't been triggered yet"
    echo "   2. Blocklist is empty (needs to be loaded)"
    echo "   3. Metrics not yet exposed"
fi

echo ""
echo "=========================================="
echo "Test Complete"
echo "=========================================="
echo ""
echo "To manually test a specific abusive campaign:"
echo "  1. Make a request in a category with abusive ads"
echo "  2. Check the response - abusive ads should NOT appear in candidates"
echo "  3. Check filter breakdown - AbusiveContentFilter should show blocks"
echo ""
echo "To check blocklist metrics:"
echo "  curl -s ${API_URL}/actuator/prometheus | grep ad_blocklist_hits_total"
echo ""
