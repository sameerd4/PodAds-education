#!/bin/bash

# Quick test script to verify a single brand logo
# Usage: ./scripts/test-logo.sh <brand-name>
# Example: ./scripts/test-logo.sh nike

if [ -z "$1" ]; then
  echo "Usage: $0 <brand-name>"
  echo "Example: $0 nike"
  echo "Example: $0 microsoft"
  exit 1
fi

BRAND=$1
URL="https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/${BRAND}.svg"

echo "Testing logo for: $BRAND"
echo "URL: $URL"
echo ""

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$URL" --max-time 5)

if [ "$HTTP_CODE" = "200" ]; then
  echo "✅ Logo exists (HTTP $HTTP_CODE)"
  echo ""
  echo "Preview (first 200 chars):"
  curl -s "$URL" | head -c 200
  echo "..."
else
  echo "❌ Logo not found (HTTP $HTTP_CODE)"
  echo ""
  echo "Try checking: https://simpleicons.org/"
  echo "Or search for alternative logo sources"
fi
