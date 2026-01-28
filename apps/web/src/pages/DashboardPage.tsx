import { useState, useEffect } from 'react';
import {
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import {
  getTotalRevenue,
  getAverageLatency,
  getRequestRate,
  getRevenueHistory,
  getLatencyHistory,
  getRequestRateHistory,
  getRequestsByCategory,
  getRequestsByTier,
} from '../lib/prometheus';

interface DashboardMetrics {
  revenue: number;
  latency: number;
  requestRate: number;
  revenueHistory: Array<{ time: number; value: number }>;
  latencyHistory: Array<{ time: number; value: number }>;
  requestRateHistory: Array<{ time: number; value: number }>;
  requestsByCategory: Array<{ category: string; count: number }>;
  requestsByTier: Array<{ tier: string; count: number }>;
}

function StatPanel({
  title,
  value,
  format,
  color = 'text-white',
}: {
  title: string;
  value: number;
  format: 'currency' | 'ms' | 'ops';
  color?: string;
}) {
  const formatValue = (val: number): string => {
    switch (format) {
      case 'currency':
        return `$${val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
      case 'ms':
        return `${val.toFixed(1)} ms`;
      case 'ops':
        return `${val.toFixed(2)} ops/s`;
      default:
        return val.toString();
    }
  };

  const getColorClass = (): string => {
    if (format === 'ms') {
      if (value < 50) return 'text-green-400';
      if (value < 100) return 'text-yellow-400';
      return 'text-red-400';
    }
    return color;
  };

  return (
    <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
      <div className="text-sm text-gray-400 mb-2">{title}</div>
      <div className={`text-4xl font-bold ${getColorClass()}`}>
        {formatValue(value)}
      </div>
    </div>
  );
}

function TimeSeriesChart({
  data,
  title,
  yAxisLabel,
  color = '#3b82f6',
  valueFormatter,
}: {
  data: Array<{ time: number; value: number }>;
  title: string;
  yAxisLabel?: string;
  color?: string;
  valueFormatter?: (value: number) => string;
}) {
  const chartData = data.map((point) => ({
    time: new Date(point.time).toLocaleTimeString(),
    value: point.value,
  }));

  const formatTooltipValue = (value: number): string => {
    if (valueFormatter) {
      return valueFormatter(value);
    }
    return value.toString();
  };

  return (
    <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
      <div className="text-sm text-gray-400 mb-4">{title}</div>
      <ResponsiveContainer width="100%" height={250}>
        <AreaChart data={chartData}>
          <defs>
            <linearGradient id={`gradient-${title}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={color} stopOpacity={0.3} />
              <stop offset="95%" stopColor={color} stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
          <XAxis
            dataKey="time"
            stroke="#9ca3af"
            style={{ fontSize: '12px' }}
            tick={{ fill: '#9ca3af' }}
          />
          <YAxis
            stroke="#9ca3af"
            style={{ fontSize: '12px' }}
            tick={{ fill: '#9ca3af' }}
            label={yAxisLabel ? { value: yAxisLabel, angle: -90, position: 'insideLeft', style: { fill: '#9ca3af' } } : undefined}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: '#1f2937',
              border: '1px solid #374151',
              borderRadius: '6px',
              color: '#fff',
            }}
            labelStyle={{ color: '#9ca3af' }}
            formatter={(value: number) => formatTooltipValue(value)}
          />
          <Area
            type="monotone"
            dataKey="value"
            stroke={color}
            strokeWidth={2}
            fill={`url(#gradient-${title})`}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}

// Category color palette - vibrant gradients
const CATEGORY_COLORS: Record<string, { start: string; end: string }> = {
  fitness: { start: '#10b981', end: '#059669' },      // Green gradient
  tech: { start: '#3b82f6', end: '#2563eb' },         // Blue gradient
  finance: { start: '#f59e0b', end: '#d97706' },      // Orange gradient
  'true-crime': { start: '#ef4444', end: '#dc2626' }, // Red gradient
  sports: { start: '#8b5cf6', end: '#7c3aed' },       // Purple gradient
  comedy: { start: '#ec4899', end: '#db2777' },       // Pink gradient
  news: { start: '#06b6d4', end: '#0891b2' },          // Cyan gradient
  education: { start: '#14b8a6', end: '#0d9488' },     // Teal gradient
};

function CategoryBarChart({ data }: { data: Array<{ category: string; count: number }> }) {
  const formatCategoryName = (category: string): string => {
    return category
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  };

  const chartData = data
    .map(item => ({
      name: formatCategoryName(item.category),
      value: item.count,
      category: item.category,
    }))
    .sort((a, b) => b.value - a.value); // Sort by count descending

  if (chartData.length === 0) {
    return (
      <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
        <div className="text-sm text-gray-400 mb-4">Requests by Category</div>
        <div className="flex items-center justify-center h-[250px] text-gray-500">
          No data available
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
      <div className="text-sm text-gray-400 mb-4">Requests by Category</div>
      <ResponsiveContainer width="100%" height={250}>
        <BarChart data={chartData} barCategoryGap="15%">
          <defs>
            {chartData.map((item, index) => {
              const colors = CATEGORY_COLORS[item.category.toLowerCase()] || { start: '#6b7280', end: '#4b5563' };
              return (
                <linearGradient key={`gradient-${index}`} id={`categoryGradient-${index}`} x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor={colors.start} stopOpacity={0.9} />
                  <stop offset="100%" stopColor={colors.end} stopOpacity={0.7} />
                </linearGradient>
              );
            })}
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#374151" opacity={0.3} />
          <XAxis
            dataKey="name"
            stroke="#9ca3af"
            style={{ fontSize: '12px' }}
            tick={{ fill: '#9ca3af' }}
            angle={-45}
            textAnchor="end"
            height={60}
          />
          <YAxis
            stroke="#9ca3af"
            style={{ fontSize: '12px' }}
            tick={{ fill: '#9ca3af' }}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: '#111827',
              border: '1px solid #374151',
              borderRadius: '8px',
              color: '#ffffff',
              boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.3)',
            }}
            labelStyle={{ 
              color: '#ffffff', 
              fontWeight: '600',
              marginBottom: '4px',
              fontSize: '13px',
            }}
            itemStyle={{
              color: '#ffffff',
              fontSize: '13px',
            }}
            formatter={(value: number) => value.toLocaleString()}
            cursor={{ fill: 'rgba(59, 130, 246, 0.1)' }}
          />
          <Bar 
            dataKey="value" 
            radius={[8, 8, 0, 0]}
            strokeWidth={0}
          >
            {chartData.map((_, index) => (
              <Cell 
                key={`cell-${index}`} 
                fill={`url(#categoryGradient-${index})`}
                style={{ 
                  filter: 'drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3))',
                  transition: 'all 0.2s ease',
                }}
              />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

const TIER_COLORS: Record<string, string> = {
  free: '#10b981',    // Green
  premium: '#f59e0b', // Orange
  unknown: '#6b7280', // Gray
};

function TierPieChart({ data }: { data: Array<{ tier: string; count: number }> }) {
  const chartData = data
    .map(item => ({
      name: item.tier.charAt(0).toUpperCase() + item.tier.slice(1),
      value: item.count,
      tier: item.tier,
    }))
    .filter(item => item.value > 0); // Only show tiers with data

  const total = chartData.reduce((sum, item) => sum + item.value, 0);

  if (chartData.length === 0) {
    return (
      <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
        <div className="text-sm text-gray-400 mb-4">Requests by Tier</div>
        <div className="flex items-center justify-center h-[250px] text-gray-500">
          No data available
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
      <div className="text-sm text-gray-400 mb-4">Requests by Tier</div>
      <ResponsiveContainer width="100%" height={250}>
        <PieChart>
          <defs>
            <linearGradient id="freeGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#10b981" stopOpacity={0.9} />
              <stop offset="100%" stopColor="#059669" stopOpacity={0.7} />
            </linearGradient>
            <linearGradient id="premiumGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#f59e0b" stopOpacity={0.9} />
              <stop offset="100%" stopColor="#d97706" stopOpacity={0.7} />
            </linearGradient>
          </defs>
          <Pie
            data={chartData}
            cx="50%"
            cy="50%"
            labelLine={false}
            label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(1)}%`}
            outerRadius={85}
            innerRadius={35}
            fill="#8884d8"
            dataKey="value"
            stroke="#1f2937"
            strokeWidth={2}
          >
            {chartData.map((entry, index) => {
              const gradientId = entry.tier.toLowerCase() === 'free' 
                ? 'freeGradient' 
                : entry.tier.toLowerCase() === 'premium' 
                ? 'premiumGradient' 
                : undefined;
              const fill = gradientId 
                ? `url(#${gradientId})` 
                : (TIER_COLORS[entry.tier.toLowerCase()] || TIER_COLORS.unknown);
              return (
                <Cell
                  key={`cell-${index}`}
                  fill={fill}
                  style={{ 
                    filter: 'drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3))',
                    transition: 'all 0.2s ease',
                  }}
                />
              );
            })}
          </Pie>
          <Tooltip
            contentStyle={{
              backgroundColor: '#111827',
              border: '1px solid #374151',
              borderRadius: '8px',
              color: '#ffffff',
              boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.3)',
            }}
            labelStyle={{ 
              color: '#ffffff', 
              fontWeight: '600',
              marginBottom: '4px',
              fontSize: '13px',
            }}
            itemStyle={{
              color: '#ffffff',
              fontSize: '13px',
            }}
            formatter={(value: number) => [
              `${value.toLocaleString()} (${total > 0 ? ((value / total) * 100).toFixed(1) : 0}%)`,
              'Requests'
            ]}
          />
          <Legend 
            wrapperStyle={{ color: '#9ca3af', fontSize: '12px' }}
            iconType="circle"
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}

export default function DashboardPage() {
  const [metrics, setMetrics] = useState<DashboardMetrics>({
    revenue: 0,
    latency: 0,
    requestRate: 0,
    revenueHistory: [],
    latencyHistory: [],
    requestRateHistory: [],
    requestsByCategory: [],
    requestsByTier: [],
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdate, setLastUpdate] = useState<Date>(new Date());

  const fetchMetrics = async () => {
    try {
      setError(null);
      
      // Get current time range (last 12 hours)
      const end = new Date();
      const start = new Date(end.getTime() - 12 * 60 * 60 * 1000);

      // Fetch all metrics in parallel
      const [
        revenue,
        latency,
        requestRate,
        revenueHistory,
        latencyHistory,
        requestRateHistory,
        requestsByCategory,
        requestsByTier,
      ] = await Promise.all([
        getTotalRevenue(),
        getAverageLatency(),
        getRequestRate(),
        getRevenueHistory(start, end),
        getLatencyHistory(start, end),
        getRequestRateHistory(start, end),
        getRequestsByCategory(),
        getRequestsByTier(),
      ]);

      setMetrics({
        revenue,
        latency,
        requestRate,
        revenueHistory,
        latencyHistory,
        requestRateHistory,
        requestsByCategory,
        requestsByTier,
      });
      
      setLastUpdate(new Date());
      setLoading(false);
    } catch (err) {
      console.error('Error fetching metrics:', err);
      setError('Failed to fetch metrics. Make sure Prometheus is running at http://localhost:9090');
      setLoading(false);
    }
  };

  useEffect(() => {
    // Initial fetch
    fetchMetrics();

    // Refresh every 10 seconds
    const interval = setInterval(fetchMetrics, 10000);

    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-400 mx-auto mb-4"></div>
          <div className="text-gray-400">Loading metrics...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 sm:gap-0">
        <h1 className="text-2xl sm:text-3xl font-bold">Main Dashboard</h1>
        <div className="text-xs sm:text-sm text-gray-400">
          Last updated: {lastUpdate.toLocaleTimeString()}
        </div>
      </div>

      {error && (
        <div className="bg-red-900/50 border border-red-700 text-red-200 px-4 py-3 rounded">
          <strong>Error:</strong> {error}
          <div className="text-sm mt-2">
            Make sure Prometheus is running: <code className="bg-gray-800 px-2 py-1 rounded">docker-compose up -d prometheus</code>
          </div>
        </div>
      )}

      {/* Top Row: 3 Stat Panels */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatPanel
          title="Total Revenue"
          value={metrics.revenue}
          format="currency"
          color="text-green-400"
        />
        <StatPanel
          title="Average Decision Latency"
          value={metrics.latency}
          format="ms"
        />
        <StatPanel
          title="Requests per Second"
          value={metrics.requestRate}
          format="ops"
          color="text-blue-400"
        />
      </div>

      {/* Middle Row: 2 Time Series Graphs */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <TimeSeriesChart
          data={metrics.revenueHistory}
          title="Total Revenue Over Time"
          yAxisLabel="USD"
          color="#10b981"
          valueFormatter={(value) => `$${value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
        />
        <TimeSeriesChart
          data={metrics.latencyHistory}
          title="Average Decision Latency Over Time"
          yAxisLabel="ms"
          color="#f59e0b"
        />
      </div>

      {/* Bottom Row: 1 Full-Width Time Series Graph */}
      <TimeSeriesChart
        data={metrics.requestRateHistory}
        title="Request Rate Over Time"
        yAxisLabel="ops/s"
        color="#3b82f6"
      />

      {/* Category & Tier Breakdown - Below Request Rate */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <CategoryBarChart data={metrics.requestsByCategory} />
        <TierPieChart data={metrics.requestsByTier} />
      </div>
    </div>
  );
}
