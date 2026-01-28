export interface MetricsSnapshot {
  fillRate: number; // 0.0 to 1.0
  latency: {
    p50: number; // milliseconds
    p95: number; // milliseconds
    p99: number; // milliseconds
  };
  errors: {
    count: number;
    rate: number; // errors per second
    topErrors: Array<{
      error: string;
      count: number;
    }>;
  };
  filterDropOffs: Array<{
    filterName: string;
    reasonCode: string;
    count: number;
  }>;
  eventStream: Array<{
    timestamp: string;
    eventType: 'decision' | 'impression' | 'complete' | 'click' | 'error';
    decisionId?: string;
    details?: string;
  }>;
}


