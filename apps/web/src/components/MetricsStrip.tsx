import { AdDecision } from '@podads/shared';
import BrandLogo from './BrandLogo';
import { BatchResults } from '../stores/useDecisionStore';

interface MetricsStripProps {
  decision: AdDecision | null;
  batchResults?: BatchResults | null;
}

export default function MetricsStrip({ decision, batchResults }: MetricsStripProps) {
  if (!decision) {
    return (
      <div className="bg-gray-800 rounded-lg p-4">
        <h3 className="text-sm font-semibold mb-2">Metrics</h3>
        <p className="text-gray-400 text-sm">Run a decision to see metrics</p>
      </div>
    );
  }

  const totalLatency = decision.stages.reduce((sum: number, s: typeof decision.stages[0]) => sum + s.latencyMs, 0);
  const fillRate = decision.winner ? 1.0 : 0.0;
  const filterDropOffs = decision.candidates
    .flatMap((c: typeof decision.candidates[0]) =>
      Object.entries(c.filterResults)
        .filter(([_, r]: [string, any]) => !r.passed)
        .map(([name, r]: [string, any]) => ({ filter: name, reason: r.reasonCode || 'unknown' }))
    )
    .reduce((acc: Record<string, number>, item: { filter: string; reason: string }) => {
      const key = `${item.filter}-${item.reason}`;
      acc[key] = (acc[key] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);

  const topDropOffs = Object.entries(filterDropOffs)
    .sort(([, a]: [string, number], [, b]: [string, number]) => b - a)
    .slice(0, 3)
    .map(([key]: [string, number]) => key.split('-')[0].replace('Filter', ''));

  // Calculate revenue: pricePaid is in cents, CPM = pricePaid/100, revenue per impression = CPM/1000
  // So: revenue per impression = (pricePaid / 100) / 1000 = pricePaid / 100000
  const singleImpressionRevenue = decision.winner?.serve.pricePaid 
    ? (decision.winner.serve.pricePaid / 100000) 
    : 0;

  // Use batch results revenue if available, otherwise show per-impression
  const revenue = batchResults?.totalRevenue ?? singleImpressionRevenue;
  const revenueLabel = batchResults ? 'Total Revenue' : 'Revenue';
  const revenueSubtext = batchResults 
    ? `${batchResults.fills} impressions`
    : 'per impression';

  return (
    <div className="bg-gray-800 rounded-lg p-4">
      <h3 className="text-sm font-semibold mb-3">Metrics Snapshot</h3>
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 sm:gap-4">
        <div>
          <div className="text-xs text-gray-400">Fill Rate</div>
          <div className="text-lg font-bold text-white">
            {(fillRate * 100).toFixed(0)}%
          </div>
        </div>
        <div>
          <div className="text-xs text-gray-400">Total Latency</div>
          <div className="text-lg font-bold text-white">{totalLatency.toFixed(1)}ms</div>
        </div>
        <div>
          <div className="text-xs text-gray-400">Candidates</div>
          <div className="text-lg font-bold text-white">
            {decision.candidates.length}
          </div>
        </div>
        <div>
          <div className="text-xs text-gray-400">Top Filter Drops</div>
          <div className="text-sm text-white">
            {topDropOffs.length > 0 ? topDropOffs.join(', ') : 'None'}
          </div>
        </div>
        <div>
          <div className="text-xs text-gray-400">{revenueLabel}</div>
          <div className="text-lg font-bold text-yellow-400">
            ${revenue.toFixed(4)}
          </div>
          <div className="text-xs text-gray-500">{revenueSubtext}</div>
        </div>
      </div>
      {decision.winner && (
        <div className="mt-3 pt-3 border-t border-gray-700">
          <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-2">
            <BrandLogo
              brandName={(decision.winner.candidate as any).brandName}
              campaignName={((decision.winner.serve as any).campaignName)}
              size={32}
            />
            <div className="text-xs text-gray-400 flex-1">
              <div className="flex flex-wrap items-center gap-1 sm:gap-2">
                <span>Winner:</span>
                <span className="text-white font-bold text-sm sm:text-base">
                  {(decision.winner.candidate as any).brandName || decision.winner.candidate.campaignId}
                </span>
                {((decision.winner.serve as any).campaignName) && (
                  <span className="text-gray-500">
                    - {((decision.winner.serve as any).campaignName)}
                  </span>
                )}
                <span className="hidden sm:inline">|</span>
                <span className="block sm:inline">Price Paid:</span>
                <span className="text-white">
                  ${((decision.winner.serve.pricePaid || 0) / 100).toFixed(2)}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

