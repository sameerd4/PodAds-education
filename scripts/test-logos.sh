#!/bin/bash

# Test all brand logos from Simple Icons CDN
# This script extracts brand names and tests their logo URLs

echo "Testing brand logos from Simple Icons CDN..."
echo ""

# Brands to test (from brandLogos.ts)
declare -a brands=(
  "nike"
  "adidas"
  "underarmour"
  "peloton"
  "lululemon"
  "gatorade"
  "fitbit"
  "reebok"
  "puma"
  "strava"
  "espn"
  "wilson"
  "rawlings"
  "newbalance"
  "mizuno"
  "apple"
  "samsung"
  "google"
  "microsoft"
  "meta"
  "amazon"
  "netflix"
  "adobe"
  "spotify"
  "tesla"
  "chase"
  "americanexpress"
  "capitalone"
  "bankofamerica"
  "paypal"
  "venmo"
  "robinhood"
  "fidelity"
  "mastercard"
  "visa"
  "audible"
  "amazonprime"
  "hbomax"
  "paramountplus"
  "discoveryplus"
  "hulu"
  "peacock"
  "appletv"
  "comedycentral"
  "siriusxm"
  "pandora"
  "iheartradio"
  "youtube"
  "thenewyorktimes"
  "thewallstreetjournal"
  "cnn"
  "bbc"
  "bloomberg"
  "reuters"
  "npr"
  "thewashingtonpost"
  "theguardian"
  "coursera"
  "udemy"
  "khanacademy"
  "masterclass"
  "linkedin"
  "skillshare"
  "duolingo"
  "babbel"
  "rosettastone"
  "edx"
)

success_count=0
fail_count=0
failed_brands=()

for brand in "${brands[@]}"; do
  # Use jsdelivr CDN format (matches the code)
  url="https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/${brand}.svg"
  
  # Use curl to check if the URL returns a valid response
  http_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" --max-time 5)
  
  if [ "$http_code" = "200" ]; then
    echo "✅ $brand - OK"
    ((success_count++))
  else
    echo "❌ $brand - FAILED (HTTP $http_code)"
    failed_brands+=("$brand")
    ((fail_count++))
  fi
done

echo ""
echo "=========================================="
echo "Summary:"
echo "  ✅ Success: $success_count"
echo "  ❌ Failed: $fail_count"
echo ""

if [ $fail_count -gt 0 ]; then
  echo "Failed brands:"
  for brand in "${failed_brands[@]}"; do
    echo "  - $brand"
  done
  echo ""
  echo "Test URLs:"
  for brand in "${failed_brands[@]}"; do
    echo "  https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/${brand}.svg"
  done
fi
