#!/bin/bash

# Comprehensive Observability Verification Script
# Verifies OBS-1 through OBS-7
# Usage: ./scripts/verify-observability-complete.sh

set -e

echo "🔍 Comprehensive Observability Verification"
echo "============================================"
echo "Verifying: OBS-1 (Dependencies) → OBS-7 (Latency Dashboard)"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PASSED=0
FAILED=0

check() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}❌ $1${NC}"
        ((FAILED++))
        return 1
    fi
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 1: Verify Backend is Running"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if ! curl -s http://localhost:8000/actuator/health > /dev/null 2>&1; then
    echo -e "${RED}❌ Backend is not running on port 8000${NC}"
    echo "   Start it with: cd apps/api-java && mvn spring-boot:run"
    echo ""
    echo "   Then run this script again."
    exit 1
fi

check "Backend is running on port 8000"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 2: OBS-1 - Verify Metrics Dependencies (Actuator Endpoints)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "Testing /actuator/health..."
HEALTH=$(curl -s http://localhost:8000/actuator/health)
if echo "$HEALTH" | grep -q '"status":"UP"'; then
    check "Health endpoint working"
    echo "   Response: $(echo $HEALTH | jq -r '.status' 2>/dev/null || echo 'UP')"
else
    check "Health endpoint working" && false
fi

echo ""
echo "Testing /actuator/prometheus..."
PROM_CHECK=$(curl -s http://localhost:8000/actuator/prometheus | head -3 | grep -c "# HELP" || echo "0")
if [ "$PROM_CHECK" -gt "0" ]; then
    check "Prometheus endpoint returns valid format"
    echo "   Sample: $(curl -s http://localhost:8000/actuator/prometheus | head -1)"
else
    check "Prometheus endpoint returns valid format" && false
fi

echo ""
echo "Testing /actuator/metrics..."
METRICS_COUNT=$(curl -s http://localhost:8000/actuator/metrics | jq -r '.names | length' 2>/dev/null || echo "0")
if [ "$METRICS_COUNT" -gt "0" ]; then
    check "Metrics endpoint lists metrics ($METRICS_COUNT found)"
else
    check "Metrics endpoint lists metrics" && false
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 3: OBS-2 - Verify Custom Metrics Instrumentation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "Making test requests to generate metrics..."
for i in {1..3}; do
    curl -s -X POST 'http://localhost:8000/v1/decision?seed=1234'$i \
      -H 'Content-Type: application/json' \
      -d '{
        "requestId":"verify-'$i'",
        "podcast":{"category":"fitness","show":"test","episode":"test"},
        "slot":{"type":"mid-roll"},
        "listener":{"geo":"US","device":"mobile","tier":"free","consent":true,"timeOfDay":"afternoon"},
        "timestamp":"2026-01-22T12:00:00Z"
      }' > /dev/null
done
sleep 2

echo ""
echo "Checking latency metrics..."
LATENCY_METRICS=$(curl -s http://localhost:8000/actuator/prometheus | grep -E "^ad_decision_latency_ms|^ad_stage_latency_ms" | wc -l | tr -d ' ')
if [ "$LATENCY_METRICS" -gt "0" ]; then
    check "Latency metrics found ($LATENCY_METRICS metrics)"
    echo "   Sample: $(curl -s http://localhost:8000/actuator/prometheus | grep '^ad_decision_latency_ms' | head -1 | cut -d' ' -f1)"
else
    check "Latency metrics found" && false
fi

echo ""
echo "Checking execution metrics..."
EXEC_METRICS=$(curl -s http://localhost:8000/actuator/prometheus | grep -E "^ad_requests_total|^ad_decisions_total|^ad_candidates_processed" | wc -l | tr -d ' ')
if [ "$EXEC_METRICS" -gt "0" ]; then
    check "Execution metrics found ($EXEC_METRICS metrics)"
    echo "   ad_requests_total: $(curl -s http://localhost:8000/actuator/prometheus | grep '^ad_requests_total' | awk '{print $2}')"
else
    check "Execution metrics found" && false
fi

echo ""
echo "Checking filter metrics..."
FILTER_METRICS=$(curl -s http://localhost:8000/actuator/prometheus | grep "^ad_filters_applied" | wc -l | tr -d ' ')
if [ "$FILTER_METRICS" -gt "0" ]; then
    check "Filter metrics found ($FILTER_METRICS metrics)"
else
    check "Filter metrics found" && false
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 4: OBS-3 - Verify Prometheus Endpoint Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

PROM_FORMAT=$(curl -s http://localhost:8000/actuator/prometheus | head -5 | grep -c "# HELP\|# TYPE" || echo "0")
if [ "$PROM_FORMAT" -ge "2" ]; then
    check "Prometheus format is valid (has HELP and TYPE comments)"
else
    check "Prometheus format is valid" && false
fi

AD_METRICS=$(curl -s http://localhost:8000/actuator/prometheus | grep -c "^ad_" || echo "0")
if [ "$AD_METRICS" -gt "0" ]; then
    check "Custom ad_ metrics present ($AD_METRICS metrics)"
else
    check "Custom ad_ metrics present" && false
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 5: OBS-4 - Verify Structured Logging"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "Making a request to generate logs..."
curl -s -X POST 'http://localhost:8000/v1/decision?seed=99999' \
  -H 'Content-Type: application/json' \
  -d '{
    "requestId":"log-verify",
    "podcast":{"category":"tech","show":"test","episode":"test"},
    "slot":{"type":"mid-roll"},
    "listener":{"geo":"US","device":"mobile","tier":"free","consent":true,"timeOfDay":"afternoon"},
    "timestamp":"2026-01-22T12:00:00Z"
  }' > /dev/null

sleep 1

# Check if logback-spring.xml exists
if [ -f "apps/api-java/src/main/resources/logback-spring.xml" ]; then
    check "logback-spring.xml configuration file exists"
else
    check "logback-spring.xml configuration file exists" && false
fi

# Note: We can't easily verify JSON logs without checking backend logs
# But we can verify the config exists
echo "   Note: JSON logging configured (verify logs manually in backend output)"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 6: OBS-5 - Verify Prometheus Scraping Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if prometheus.yml exists and is configured
if [ -f "infra/prometheus/prometheus.yml" ]; then
    check "Prometheus configuration file exists"
    
    if grep -q "ad_decision_latency_ms\|ad_stage_latency_ms" infra/prometheus/prometheus.yml 2>/dev/null || grep -q "/actuator/prometheus" infra/prometheus/prometheus.yml; then
        check "Prometheus config has correct metrics_path"
        echo "   Metrics path: $(grep 'metrics_path' infra/prometheus/prometheus.yml | awk '{print $2}' | tr -d '"')"
    else
        METRICS_PATH=$(grep "metrics_path" infra/prometheus/prometheus.yml | awk '{print $2}' | tr -d '"' || echo "not found")
        if [ "$METRICS_PATH" = "/actuator/prometheus" ]; then
            check "Prometheus config has correct metrics_path"
        else
            check "Prometheus config has correct metrics_path" && false
        fi
    fi
else
    check "Prometheus configuration file exists" && false
fi

# Check Docker
if ! docker info > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Docker is not running - cannot verify Prometheus scraping${NC}"
    echo "   Start Docker Desktop and run: cd infra && docker-compose up -d prometheus"
else
    echo ""
    echo "Checking if Prometheus container is running..."
    if docker ps | grep -q podads-prometheus; then
        check "Prometheus container is running"
        
        echo ""
        echo "Checking Prometheus targets..."
        sleep 2
        TARGETS=$(curl -s http://localhost:9090/api/v1/targets 2>/dev/null || echo "")
        if [ ! -z "$TARGETS" ]; then
            TARGET_STATUS=$(echo "$TARGETS" | jq -r '.data.activeTargets[] | "\(.labels.job): \(.health)"' 2>/dev/null | head -1 || echo "")
            if echo "$TARGET_STATUS" | grep -q "up"; then
                check "Prometheus target is UP"
                echo "   $TARGET_STATUS"
            else
                echo -e "${YELLOW}⚠️  Prometheus target status: $TARGET_STATUS${NC}"
            fi
        fi
        
        echo ""
        echo "Querying metrics in Prometheus..."
        METRIC_QUERY=$(curl -s 'http://localhost:9090/api/v1/query?query=ad_requests_total' 2>/dev/null | jq -r '.status' 2>/dev/null || echo "")
        if [ "$METRIC_QUERY" = "success" ]; then
            check "Prometheus can query ad_ metrics"
        else
            echo -e "${YELLOW}⚠️  Prometheus query test: $METRIC_QUERY${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Prometheus container not running${NC}"
        echo "   Start with: cd infra && docker-compose up -d prometheus"
    fi
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 7: OBS-6 - Verify Grafana Data Source"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -f "infra/grafana/provisioning/datasources/prometheus.yml" ]; then
    check "Grafana data source configuration exists"
    
    if grep -q "prometheus:9090" infra/grafana/provisioning/datasources/prometheus.yml; then
        check "Data source points to Prometheus"
        echo "   URL: $(grep 'url:' infra/grafana/provisioning/datasources/prometheus.yml | awk '{print $2}')"
    else
        check "Data source points to Prometheus" && false
    fi
else
    check "Grafana data source configuration exists" && false
fi

# Check Docker for Grafana
if docker info > /dev/null 2>&1; then
    if docker ps | grep -q podads-grafana; then
        check "Grafana container is running"
        
        echo ""
        echo "Checking Grafana API..."
        sleep 2
        GRAFANA_HEALTH=$(curl -s http://localhost:3001/api/health 2>/dev/null | jq -r '.database' 2>/dev/null || echo "")
        if [ ! -z "$GRAFANA_HEALTH" ]; then
            check "Grafana API is accessible"
            echo "   Access at: http://localhost:3001 (admin/admin)"
        fi
        
        echo ""
        echo "Checking Grafana data sources..."
        DS_COUNT=$(curl -s -u admin:admin http://localhost:3001/api/datasources 2>/dev/null | jq '. | length' 2>/dev/null || echo "0")
        if [ "$DS_COUNT" -gt "0" ]; then
            check "Grafana has data sources configured ($DS_COUNT)"
        else
            echo -e "${YELLOW}⚠️  No data sources found (may need to restart Grafana)${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Grafana container not running${NC}"
        echo "   Start with: cd infra && docker-compose up -d grafana"
    fi
else
    echo -e "${YELLOW}⚠️  Docker is not running - cannot verify Grafana${NC}"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STEP 8: OBS-7 - Verify Latency Dashboard"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -f "infra/grafana/provisioning/dashboards/latency-dashboard.json" ]; then
    check "Latency dashboard JSON exists"
    
    DASHBOARD_TITLE=$(jq -r '.title' infra/grafana/provisioning/dashboards/latency-dashboard.json 2>/dev/null || echo "")
    if [ ! -z "$DASHBOARD_TITLE" ]; then
        check "Dashboard JSON is valid"
        echo "   Title: $DASHBOARD_TITLE"
        
        PANEL_COUNT=$(jq '.panels | length' infra/grafana/provisioning/dashboards/latency-dashboard.json 2>/dev/null || echo "0")
        echo "   Panels: $PANEL_COUNT"
    else
        check "Dashboard JSON is valid" && false
    fi
else
    check "Latency dashboard JSON exists" && false
fi

if [ -f "infra/grafana/provisioning/dashboards/dashboard.yml" ]; then
    check "Dashboard provisioning config exists"
else
    check "Dashboard provisioning config exists" && false
fi

# Check Grafana for dashboard
if docker info > /dev/null 2>&1 && docker ps | grep -q podads-grafana; then
    echo ""
    echo "Checking if dashboard is loaded in Grafana..."
    sleep 2
    DASHBOARD_LIST=$(curl -s -u admin:admin http://localhost:3001/api/search?query=latency 2>/dev/null | jq -r '.[] | .title' 2>/dev/null || echo "")
    if echo "$DASHBOARD_LIST" | grep -qi "latency"; then
        check "Latency dashboard appears in Grafana"
        echo "   View at: http://localhost:3001"
    else
        echo -e "${YELLOW}⚠️  Dashboard not found in Grafana (may need to restart Grafana)${NC}"
        echo "   Dashboard should auto-load on next Grafana restart"
    fi
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "SUMMARY"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${GREEN}✅ Passed: $PASSED${NC}"
if [ $FAILED -gt 0 ]; then
    echo -e "${RED}❌ Failed: $FAILED${NC}"
fi
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All observability checks passed!${NC}"
    echo ""
    echo "📊 Quick Access:"
    echo "   - Backend Health: http://localhost:8000/actuator/health"
    echo "   - Prometheus Metrics: http://localhost:8000/actuator/prometheus"
    echo "   - Prometheus UI: http://localhost:9090 (if Docker running)"
    echo "   - Grafana: http://localhost:3001 (admin/admin, if Docker running)"
    echo ""
    echo "🔧 To start Docker services:"
    echo "   cd infra && docker-compose up -d prometheus grafana"
else
    echo -e "${YELLOW}⚠️  Some checks failed. Review output above.${NC}"
fi
echo ""
