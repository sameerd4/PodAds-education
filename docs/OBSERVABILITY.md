# PodAds Lab - Observability Guide

**Complete guide to monitoring, alerting, and troubleshooting the PodAds Lab ad serving system.**

---

## Table of Contents

1. [Overview](#overview)
2. [Accessing Dashboards](#accessing-dashboards)
3. [Metrics Reference](#metrics-reference)
4. [Dashboards Guide](#dashboards-guide)
5. [Alerting](#alerting)
6. [Querying Metrics](#querying-metrics)
7. [Troubleshooting](#troubleshooting)
8. [On-Call Procedures](#on-call-procedures)

---

## Overview

PodAds Lab uses a comprehensive observability stack:

- **Metrics**: Prometheus + Micrometer (Spring Boot Actuator)
- **Visualization**: Grafana dashboards
- **Logging**: Structured JSON logs with correlation IDs
- **Alerting**: Prometheus alert rules + Grafana notifications

### Architecture

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  PodAds API │────▶│  Prometheus  │────▶│   Grafana   │
│  (Port 8000)│     │  (Port 9090) │     │  (Port 3001)│
└─────────────┘     └──────────────┘     └─────────────┘
      │                    │                    │
      │                    │                    │
      └────────────────────┴────────────────────┘
                    Alert Rules
```

---

## Accessing Dashboards

### Prerequisites

1. **Start Docker services:**
   ```bash
   cd infra
   docker-compose up -d prometheus grafana
   ```

2. **Verify services are running:**
   ```bash
   # Prometheus
   curl http://localhost:9090/-/healthy
   
   # Grafana
   curl http://localhost:3001/api/health
   ```

### Grafana Access

- **URL**: http://localhost:3001
- **Username**: `admin`
- **Password**: `admin`
- **Default Dashboards**: Automatically provisioned on first login

### Prometheus Access

- **URL**: http://localhost:9090
- **Query Interface**: http://localhost:9090/graph
- **Alerts**: http://localhost:9090/alerts
- **Targets**: http://localhost:9090/targets

---

## Metrics Reference

### Latency Metrics

| Metric | Type | Description | Tags |
|--------|------|-------------|------|
| `ad_decision_latency_ms` | Timer | Total decision latency | - |
| `ad_stage_latency_ms` | Timer | Per-stage latency | `stage` (Request, Sourcing, Filters, Auction, Serve) |

### Execution Metrics

| Metric | Type | Description | Tags |
|--------|------|-------------|------|
| `ad_requests_total` | Counter | Total ad requests | - |
| `ad_decisions_total` | Counter | Total decisions | `outcome` (fill/no_fill), `category`, `slot_type` |
| `ad_candidates_processed` | DistributionSummary | Candidates per request | - |
| `ad_filters_applied_total` | Counter | Filter applications | `filter_name`, `passed` (true/false) |

### Business Metrics

| Metric | Type | Description | Tags |
|--------|------|-------------|------|
| `ad_campaign_served_total` | Counter | Ads served per campaign | `campaign_id`, `category`, `slot_type` |
| `ad_category_fills_total` | Counter | Fills by category | `category`, `slot_type` |
| `ad_slot_decisions_total` | Counter | Decisions by slot type | `slot_type`, `outcome` |

### Error Metrics

| Metric | Type | Description | Tags |
|--------|------|-------------|------|
| `ad_errors_total` | Counter | Total errors | `error_type`, `stage` |
| `ad_http_errors_total` | Counter | HTTP errors | `status_code` |

---

## Dashboards Guide

### 1. Latency Dashboard

**Purpose**: Monitor decision and stage-level latency

**Key Panels**:
- **Total Decision Latency**: p50, p95, p99 percentiles
- **Average Decision Latency**: Overall average
- **Latency Over Time**: Trend visualization
- **Stage Latency Breakdown**: Per-stage comparison
- **Stage Latency Over Time**: Stage trends
- **Latency Distribution**: Heatmap

**When to Check**: 
- User reports slow ad serving
- p95 latency > 100ms
- p99 latency > 200ms

### 2. Execution Metrics Dashboard

**Purpose**: Monitor business KPIs and execution performance

**Key Panels**:
- **Overall Fill Rate**: Single gauge (target: >80%)
- **Fill vs No-Fill**: Pie chart showing fill ratio
- **Fill Rate by Category**: Bar chart + table
- **Decisions by Category**: Volume by category
- **Fill Rate Over Time**: Trend by category
- **Request Rate**: RPS monitoring
- **Filter Drop Rates**: Which filters drop most ads

**When to Check**:
- Fill rate drops below 80%
- Specific category underperforming
- Sudden drop in request rate

### 3. Error Metrics Dashboard

**Purpose**: Monitor errors and failures

**Key Panels**:
- **Error Rate**: Overall error percentage
- **Total Errors**: Cumulative count
- **HTTP Error Rate**: HTTP-level errors
- **Error Rate Over Time**: Trend
- **Errors by Type**: Breakdown by error type
- **HTTP Errors by Status Code**: 4xx/5xx breakdown
- **Errors by Stage**: Which stage fails most

**When to Check**:
- Error rate > 1%
- User reports failures
- HTTP 5xx errors

### 4. Health & Well-being Dashboard

**Purpose**: High-level system health overview

**Key Panels**:
- **System Health**: Overall status
- **Uptime**: Service uptime
- **Request Rate**: Current RPS
- **Fill Rate**: Current fill rate
- **Error Rate**: Current error rate
- **Current Latency**: p95 latency
- **Active Alerts**: Count of firing alerts
- **Trends**: Fill rate, latency, request rate trends

**When to Check**:
- On-call shift start
- After deployments
- When alerts fire

---

## Alerting

### Alert Rules

Alert rules are defined in `infra/prometheus/alert_rules.yml`:

#### High Latency (Warning)
- **Condition**: p95 latency > 100ms for 5 minutes
- **Severity**: Warning
- **Action**: Investigate latency spikes

#### Critical Latency (Critical)
- **Condition**: p99 latency > 200ms for 2 minutes
- **Severity**: Critical
- **Action**: Immediate investigation

#### Low Fill Rate (Warning)
- **Condition**: Fill rate < 80% for 5 minutes
- **Severity**: Warning
- **Action**: Check filter drop rates, campaign availability

#### Critical Low Fill Rate (Critical)
- **Condition**: Fill rate < 50% for 2 minutes
- **Severity**: Critical
- **Action**: Emergency investigation

#### High Error Rate (Warning)
- **Condition**: Error rate > 1% for 5 minutes
- **Severity**: Warning
- **Action**: Check error logs, investigate failures

#### Critical High Error Rate (Critical)
- **Condition**: Error rate > 5% for 2 minutes
- **Severity**: Critical
- **Action**: Emergency response

#### Service Down (Critical)
- **Condition**: Service unavailable for 1 minute
- **Severity**: Critical
- **Action**: Check service health, restart if needed

#### No Requests (Warning)
- **Condition**: No requests for 10 minutes
- **Severity**: Warning
- **Action**: Check upstream systems, verify traffic

### Viewing Alerts

1. **Prometheus**: http://localhost:9090/alerts
2. **Grafana**: Alerts → Alert Rules

### Alert Notifications

Configured notification channels:
- **Email**: `oncall@podads-lab.local` (for demo)
- **Webhook**: `http://localhost:8080/webhook` (for testing)

To configure real notifications:
1. Go to Grafana → Alerting → Notification channels
2. Add email/Slack/PagerDuty channel
3. Update alert rules to use new channel

---

## Querying Metrics

### Prometheus Query Language (PromQL)

#### Basic Queries

```promql
# Request rate
rate(ad_requests_total[5m])

# Fill rate
sum(rate(ad_decisions_total{outcome="fill"}[5m])) / sum(rate(ad_decisions_total[5m])) * 100

# Error rate
rate(ad_errors_total[5m])

# p95 latency
histogram_quantile(0.95, rate(ad_decision_latency_ms_bucket[5m]))
```

#### Filtered Queries

```promql
# Fill rate by category
sum(rate(ad_decisions_total{outcome="fill", category="fitness"}[5m])) / 
sum(rate(ad_decisions_total{category="fitness"}[5m])) * 100

# Latency by stage
histogram_quantile(0.95, rate(ad_stage_latency_ms_bucket{stage="Filters"}[5m]))

# Filter drop rate
rate(ad_filters_applied_total{passed="false", filter_name="BudgetRemainingFilter"}[5m])
```

#### Business Metrics

```promql
# Campaign performance
sum(rate(ad_campaign_served_total{campaign_id="camp-001"}[5m]))

# Slot-type fill rate
sum(rate(ad_slot_decisions_total{slot_type="mid-roll", outcome="fill"}[5m])) / 
sum(rate(ad_slot_decisions_total{slot_type="mid-roll"}[5m])) * 100

# Category efficiency
sum(rate(ad_category_fills_total{category="tech"}[5m]))
```

### Using Grafana Explore

1. Go to Grafana → Explore
2. Select Prometheus data source
3. Enter PromQL query
4. View results in table/graph format

---

## Troubleshooting

### No Metrics Appearing

**Symptoms**: Dashboards show "No data"

**Diagnosis**:
```bash
# Check Prometheus is scraping
curl http://localhost:9090/api/v1/targets

# Check metrics endpoint
curl http://localhost:8000/actuator/prometheus | head -20

# Check service is running
curl http://localhost:8000/actuator/health
```

**Solutions**:
1. Ensure backend is running on port 8000
2. Verify Prometheus can reach `host.docker.internal:8000`
3. Check Prometheus targets page for errors
4. Restart Prometheus: `docker-compose restart prometheus`

### High Latency

**Symptoms**: p95 latency > 100ms

**Diagnosis**:
1. Check Latency Dashboard → Stage Latency Breakdown
2. Identify slowest stage
3. Check logs for that stage
4. Review filter drop rates (high drops = more processing)

**Solutions**:
- **Filters stage slow**: Too many candidates, optimize sourcing
- **Auction stage slow**: Too many candidates passing filters
- **Sourcing stage slow**: Fixture loading issue

### Low Fill Rate

**Symptoms**: Fill rate < 80%

**Diagnosis**:
1. Check Execution Dashboard → Filter Drop Rates
2. Identify which filters drop most ads
3. Check category-specific fill rates
4. Review campaign availability

**Solutions**:
- **BudgetRemainingFilter**: Campaigns out of budget
- **PacingGateFilter**: Pacing limits hit
- **CategoryMatchFilter**: No campaigns for category
- **ScheduleWindowFilter**: Campaigns expired/not started

### High Error Rate

**Symptoms**: Error rate > 1%

**Diagnosis**:
1. Check Error Dashboard → Errors by Type
2. Check Error Dashboard → Errors by Stage
3. Review application logs
4. Check HTTP error codes

**Solutions**:
- **JSON parsing errors**: Invalid request format
- **NullPointerException**: Missing data in fixtures
- **HTTP 400**: Request validation failures
- **HTTP 500**: Application errors, check logs

### Alerts Not Firing

**Symptoms**: Conditions met but no alerts

**Diagnosis**:
```bash
# Check alert rules loaded
curl http://localhost:9090/api/v1/rules

# Check alert evaluation
curl http://localhost:9090/api/v1/alerts
```

**Solutions**:
1. Verify `alert_rules.yml` is mounted in Prometheus
2. Check Prometheus logs: `docker logs podads-prometheus`
3. Verify rule syntax is correct
4. Ensure evaluation interval is appropriate

---

## On-Call Procedures

### Shift Start Checklist

1. **Check Health Dashboard**:
   - System health: Green
   - Fill rate: > 80%
   - Error rate: < 1%
   - Latency: p95 < 100ms

2. **Review Active Alerts**:
   - Go to Grafana → Alerting → Alert Rules
   - Check for firing alerts
   - Review alert history

3. **Verify Services**:
   ```bash
   # Backend health
   curl http://localhost:8000/actuator/health
   
   # Prometheus
   curl http://localhost:9090/-/healthy
   
   # Grafana
   curl http://localhost:3001/api/health
   ```

### Incident Response

#### High Latency Incident

1. **Acknowledge**: Check Latency Dashboard
2. **Investigate**: Identify slow stage
3. **Mitigate**: 
   - If Filters: Check filter drop rates
   - If Auction: Check candidate count
   - If Sourcing: Check fixture loading
4. **Escalate**: If p99 > 500ms for > 5 minutes

#### Low Fill Rate Incident

1. **Acknowledge**: Check Execution Dashboard
2. **Investigate**: 
   - Check Filter Drop Rates panel
   - Check category-specific fill rates
   - Review campaign availability
3. **Mitigate**:
   - If BudgetRemainingFilter: Check campaign budgets
   - If CategoryMatchFilter: Verify campaigns exist for category
   - If ScheduleWindowFilter: Check campaign dates
4. **Escalate**: If fill rate < 50% for > 10 minutes

#### High Error Rate Incident

1. **Acknowledge**: Check Error Dashboard
2. **Investigate**:
   - Check Errors by Type panel
   - Check Errors by Stage panel
   - Review application logs
3. **Mitigate**:
   - If JSON parsing: Check request format
   - If NullPointerException: Check fixture data
   - If HTTP 500: Check application logs
4. **Escalate**: If error rate > 5% for > 5 minutes

#### Service Down Incident

1. **Acknowledge**: Check Health Dashboard
2. **Investigate**:
   ```bash
   # Check service status
   curl http://localhost:8000/actuator/health
   
   # Check logs
   docker logs <container-name>
   ```
3. **Mitigate**:
   - Restart service if needed
   - Check resource usage (CPU, memory)
   - Verify dependencies (Postgres, Redis)
4. **Escalate**: If service down > 5 minutes

### Post-Incident

1. **Document**: 
   - Incident timeline
   - Root cause
   - Resolution steps
   - Prevention measures

2. **Update**:
   - Alert thresholds if needed
   - Dashboard queries if needed
   - Runbook procedures

---

## Quick Reference

### Useful Commands

```bash
# Start observability stack
cd infra && docker-compose up -d prometheus grafana

# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Check alerts
curl http://localhost:9090/api/v1/alerts

# Query metrics
curl 'http://localhost:9090/api/v1/query?query=rate(ad_requests_total[5m])'

# View Prometheus logs
docker logs podads-prometheus

# View Grafana logs
docker logs podads-grafana

# Restart services
docker-compose restart prometheus grafana
```

### Dashboard URLs

- **Grafana**: http://localhost:3001
- **Prometheus**: http://localhost:9090
- **Prometheus Alerts**: http://localhost:9090/alerts
- **Prometheus Targets**: http://localhost:9090/targets

### Key Metrics Endpoints

- **Actuator Health**: http://localhost:8000/actuator/health
- **Actuator Metrics**: http://localhost:8000/actuator/metrics
- **Prometheus Export**: http://localhost:8000/actuator/prometheus

---

**Last Updated**: 2026-01-23  
**Version**: 1.0
