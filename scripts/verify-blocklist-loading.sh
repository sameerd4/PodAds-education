#!/bin/bash

# Quick verification script to check if blocklist is loading

echo "=========================================="
echo "Blocklist Loading Verification"
echo "=========================================="
echo ""

# Get script directory and project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"
BLOCKLIST_FILE="$PROJECT_ROOT/apps/api-java/src/main/resources/fixtures/blocklist.json"

cd "$PROJECT_ROOT"

# Check 1: File exists
echo "✅ Step 1: Checking blocklist.json exists..."
if [ -f "$BLOCKLIST_FILE" ]; then
    echo "   File exists: $BLOCKLIST_FILE"
else
    echo "   ❌ File not found: $BLOCKLIST_FILE"
    exit 1
fi

# Check 2: Valid JSON
echo ""
echo "✅ Step 2: Validating JSON structure..."
if python3 -m json.tool "$BLOCKLIST_FILE" > /dev/null 2>&1; then
    echo "   Valid JSON"
else
    echo "   ❌ Invalid JSON!"
    exit 1
fi

# Check 3: Count entries
echo ""
echo "✅ Step 3: Counting blocklist entries..."
TOTAL=$(python3 -c "
import json
with open('$BLOCKLIST_FILE') as f:
    data = json.load(f)
    total = sum(len(source.get('entries', [])) for source in data['sources'].values())
    print(total)
")
echo "   Total entries: $TOTAL"

# Check 4: Sample campaign IDs
echo ""
echo "✅ Step 4: Sample blocked campaign IDs:"
python3 -c "
import json
with open('$BLOCKLIST_FILE') as f:
    data = json.load(f)
    campaigns = set()
    for source in data['sources'].values():
        for entry in source.get('entries', []):
            if entry.get('campaignId'):
                campaigns.add(entry['campaignId'])
    sorted_camps = sorted(campaigns)
    for i, camp_id in enumerate(sorted_camps[:5], 1):
        print(f'   {i}. {camp_id}')
    if len(sorted_camps) > 5:
        print(f'   ... and {len(sorted_camps) - 5} more')
" 2>/dev/null

# Check 5: Backend status
echo ""
echo "✅ Step 5: Checking backend status..."
if curl -s http://localhost:8000/health > /dev/null 2>&1; then
    echo "   Backend is running"
    echo ""
    echo "⚠️  IMPORTANT: If you just updated the filter code, you MUST restart the backend!"
    echo "   The @PostConstruct method only runs on startup."
    echo ""
    echo "   To restart:"
    echo "   1. Stop backend (Ctrl+C)"
    echo "   2. cd apps/api-java && mvn spring-boot:run"
    echo ""
    echo "   Then check backend logs for:"
    echo "   - 'AbusiveContentFilter initialized'"
    echo "   - 'Blocklist loaded successfully' with campaign/creative counts"
else
    echo "   Backend is not running"
    echo "   Start it with: cd apps/api-java && mvn spring-boot:run"
fi

echo ""
echo "=========================================="
echo "Verification Complete"
echo "=========================================="
