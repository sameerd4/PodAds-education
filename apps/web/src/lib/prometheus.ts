/**
 * Prometheus query utilities
 * In production, queries via backend proxy to avoid CORS issues
 * In local dev, queries Prometheus directly
 */

// Determine base URL: use backend proxy in production, direct Prometheus in dev
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000';
const PROMETHEUS_URL = import.meta.env.VITE_PROMETHEUS_URL || 'http://localhost:9090';

// Use backend proxy if API_URL is set (production), otherwise use direct Prometheus (local dev)
const isProduction = !!import.meta.env.VITE_API_URL;
const PROMETHEUS_BASE = isProduction 
  ? `${API_BASE_URL}/api/metrics`
  : PROMETHEUS_URL;

export interface PrometheusQueryResult {
  value: [number, string]; // [timestamp, value]
}

export interface PrometheusRangeResult {
  values: Array<[number, string]>; // Array of [timestamp, value]
}

/**
 * Query Prometheus for a single metric (instant query)
 */
export async function queryPrometheus(query: string): Promise<number> {
  try {
    const response = await fetch(
      `${PROMETHEUS_BASE}/query?query=${encodeURIComponent(query)}`
    );
    
    if (!response.ok) {
      console.error(`Prometheus query failed: ${response.status}`);
      return 0;
    }
    
    const data = await response.json();
    
    if (data.status === 'success' && data.data.result.length > 0) {
      const value = parseFloat(data.data.result[0].value[1]);
      return isNaN(value) ? 0 : value;
    }
    
    return 0;
  } catch (error) {
    console.error('Error querying Prometheus:', error);
    return 0;
  }
}

/**
 * Query Prometheus for a metric over a time range
 */
export async function queryRange(
  query: string,
  start: Date,
  end: Date,
  step: string = '15s'
): Promise<Array<{ time: number; value: number }>> {
  try {
    const startSeconds = Math.floor(start.getTime() / 1000);
    const endSeconds = Math.floor(end.getTime() / 1000);
    
    const response = await fetch(
      `${PROMETHEUS_BASE}/query_range?` +
      `query=${encodeURIComponent(query)}&` +
      `start=${startSeconds}&` +
      `end=${endSeconds}&` +
      `step=${step}`
    );
    
    if (!response.ok) {
      console.error(`Prometheus range query failed: ${response.status}`);
      return [];
    }
    
    const data = await response.json();
    
    if (data.status === 'success' && data.data.result.length > 0) {
      const result = data.data.result[0];
      return result.values.map(([timestamp, value]: [number, string]) => ({
        time: timestamp * 1000, // Convert to milliseconds
        value: parseFloat(value) || 0,
      }));
    }
    
    return [];
  } catch (error) {
    console.error('Error querying Prometheus range:', error);
    return [];
  }
}

/**
 * Get total revenue
 */
export async function getTotalRevenue(): Promise<number> {
  return queryPrometheus('sum(ad_revenue_total)');
}

/**
 * Get average latency in milliseconds
 */
export async function getAverageLatency(): Promise<number> {
  const query = 
    'rate(ad_decision_latency_ms_seconds_sum[5m]) / rate(ad_decision_latency_ms_seconds_count[5m]) * 1000';
  return queryPrometheus(query);
}

/**
 * Get requests per second
 */
export async function getRequestRate(): Promise<number> {
  return queryPrometheus('sum(rate(ad_requests_total[5m]))');
}

/**
 * Get revenue over time
 */
export async function getRevenueHistory(
  start: Date,
  end: Date
): Promise<Array<{ time: number; value: number }>> {
  // Use 1 minute step for 12 hour range to get good resolution without too many points
  const timeRangeMs = end.getTime() - start.getTime();
  const step = timeRangeMs > 6 * 60 * 60 * 1000 ? '1m' : '15s'; // 1 minute for > 6 hours, 15s for shorter
  return queryRange('sum(ad_revenue_total)', start, end, step);
}

/**
 * Get latency over time
 */
export async function getLatencyHistory(
  start: Date,
  end: Date
): Promise<Array<{ time: number; value: number }>> {
  const query = 
    'rate(ad_decision_latency_ms_seconds_sum[5m]) / rate(ad_decision_latency_ms_seconds_count[5m]) * 1000';
  // Use 1 minute step for 12 hour range to get good resolution without too many points
  const timeRangeMs = end.getTime() - start.getTime();
  const step = timeRangeMs > 6 * 60 * 60 * 1000 ? '1m' : '15s'; // 1 minute for > 6 hours, 15s for shorter
  return queryRange(query, start, end, step);
}

/**
 * Get request rate over time
 */
export async function getRequestRateHistory(
  start: Date,
  end: Date
): Promise<Array<{ time: number; value: number }>> {
  // Use 1 minute step for 12 hour range to get good resolution without too many points
  const timeRangeMs = end.getTime() - start.getTime();
  const step = timeRangeMs > 6 * 60 * 60 * 1000 ? '1m' : '15s'; // 1 minute for > 6 hours, 15s for shorter
  return queryRange('sum(rate(ad_requests_total[5m]))', start, end, step);
}

/**
 * Get requests grouped by category
 * Handles both tagged metrics (with category label) and untagged historical metrics
 */
export async function getRequestsByCategory(): Promise<Array<{ category: string; count: number }>> {
  try {
    // Query: sum by category, including untagged metrics
    // This will group by category label, and untagged metrics will have empty category
    const query = 'sum by (category) (ad_requests_total)';
    const response = await fetch(
      `${PROMETHEUS_BASE}/query?query=${encodeURIComponent(query)}`
    );
    
    if (!response.ok) {
      console.error(`Prometheus query failed: ${response.status}`);
      return [];
    }
    
    const data = await response.json();
    
    if (data.status === 'success' && data.data.result.length > 0) {
      const results: Array<{ category: string; count: number }> = data.data.result
        .map((r: any): { category: string; count: number } => {
          // Handle both tagged (has category) and untagged (no category label) metrics
          const category = r.metric.category && r.metric.category !== '' 
            ? r.metric.category 
            : 'unknown';
          return {
            category,
            count: parseFloat(r.value[1]) || 0,
          };
        })
        .filter((item: { category: string; count: number }) => item.count > 0); // Only return categories with data
      
      // If we only have "unknown", that means backend hasn't been restarted yet
      // or all requests are untagged (historical data)
      return results;
    }
    
    return [];
  } catch (error) {
    console.error('Error querying requests by category:', error);
    return [];
  }
}

/**
 * Get requests grouped by tier (free vs premium)
 * Handles both tagged metrics (with tier label) and untagged historical metrics
 */
export async function getRequestsByTier(): Promise<Array<{ tier: string; count: number }>> {
  try {
    // Query: sum by tier, including untagged metrics
    // This will group by tier label, and untagged metrics will have empty tier
    const query = 'sum by (tier) (ad_requests_total)';
    const response = await fetch(
      `${PROMETHEUS_BASE}/query?query=${encodeURIComponent(query)}`
    );
    
    if (!response.ok) {
      console.error(`Prometheus query failed: ${response.status}`);
      return [];
    }
    
    const data = await response.json();
    
    if (data.status === 'success' && data.data.result.length > 0) {
      const results: Array<{ tier: string; count: number }> = data.data.result
        .map((r: any): { tier: string; count: number } => {
          // Handle both tagged (has tier) and untagged (no tier label) metrics
          const tier = r.metric.tier && r.metric.tier !== '' 
            ? r.metric.tier 
            : 'unknown';
          return {
            tier,
            count: parseFloat(r.value[1]) || 0,
          };
        })
        .filter((item: { tier: string; count: number }) => item.count > 0); // Only return tiers with data
      
      // If we only have "unknown", that means backend hasn't been restarted yet
      // or all requests are untagged (historical data)
      return results;
    }
    
    return [];
  } catch (error) {
    console.error('Error querying requests by tier:', error);
    return [];
  }
}
