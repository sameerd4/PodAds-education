import { AdDecision } from '@podads/shared';
import BrandLogo from './BrandLogo';

interface ExplainDrawerProps {
  decision: AdDecision;
  onClose: () => void;
}

export default function ExplainDrawer({ decision, onClose }: ExplainDrawerProps) {
  if (!decision.winner) {
    return null;
  }

  const winner = decision.winner.candidate;
  const runnerUp = decision.candidates[1];

  // Top 3 factors why winner won
  const winnerFactors = [
    {
      factor: 'Bid CPM',
      value: `$${(winner.score.bidCpm / 100).toFixed(2)}`,
      impact: 'High',
    },
    {
      factor: 'Match Score',
      value: `${(winner.score.matchScore * 100).toFixed(0)}%`,
      impact: winner.score.matchScore > 0.8 ? 'High' : 'Medium',
    },
    {
      factor: 'Pacing Multiplier',
      value: `${(winner.score.pacingMultiplier * 100).toFixed(0)}%`,
      impact: winner.score.pacingMultiplier > 0.9 ? 'High' : 'Medium',
    },
  ];

  // Why runner-up lost
  const runnerUpReasons = runnerUp
    ? [
        {
          reason: 'Lower Final Score',
          details: `$${(runnerUp.score.finalScore / 100).toFixed(2)} vs $${(winner.score.finalScore / 100).toFixed(2)}`,
        },
        ...(runnerUp.score.bidCpm < winner.score.bidCpm
          ? [
              {
                reason: 'Lower Bid',
                details: `$${(runnerUp.score.bidCpm / 100).toFixed(2)} vs $${(winner.score.bidCpm / 100).toFixed(2)}`,
              },
            ]
          : []),
      ]
    : [];

  // Most impactful filter (by drop count)
  const filterDropCounts = decision.candidates.reduce((acc: Record<string, number>, c: typeof decision.candidates[0]) => {
    Object.entries(c.filterResults).forEach(([name, result]: [string, any]) => {
      if (!result.passed) {
        acc[name] = (acc[name] || 0) + 1;
      }
    });
    return acc;
  }, {} as Record<string, number>);

  const topFilter = Object.entries(filterDropCounts)
    .sort(([, a]: [string, number], [, b]: [string, number]) => b - a)[0];

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-gray-800 rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-3">
            <BrandLogo
              brandName={(winner as any).brandName}
              campaignName={((decision.winner.serve as any).campaignName)}
              size={48}
            />
            <div>
              <h2 className="text-2xl font-bold">Explain Winner</h2>
              {(winner as any).brandName && (
                <div className="text-lg text-green-400 font-semibold mt-1">
                  {(winner as any).brandName}
                  {((decision.winner.serve as any).campaignName) && (
                    <span className="text-gray-400 text-sm ml-2">
                      - {((decision.winner.serve as any).campaignName)}
                    </span>
                  )}
                </div>
              )}
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white text-2xl"
          >
            ×
          </button>
        </div>

        <div className="space-y-6">
          {/* Why Winner Won */}
          <div>
            <h3 className="text-lg font-semibold mb-3 text-green-400">
              Why Winner Won (Top 3 Factors)
            </h3>
            <div className="space-y-2">
              {winnerFactors.map((factor, index) => (
                <div
                  key={index}
                  className="bg-gray-700 rounded p-3 flex items-center justify-between"
                >
                  <div>
                    <div className="font-medium">{factor.factor}</div>
                    <div className="text-sm text-gray-400">{factor.value}</div>
                  </div>
                  <div
                    className={`px-2 py-1 rounded text-xs ${
                      factor.impact === 'High'
                        ? 'bg-green-600 text-white'
                        : 'bg-yellow-600 text-white'
                    }`}
                  >
                    {factor.impact} Impact
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Why Runner-Up Lost */}
          {runnerUp && (
            <div>
              <h3 className="text-lg font-semibold mb-3 text-red-400">
                Why Runner-Up Lost
              </h3>
              <div className="space-y-2">
                {runnerUpReasons.map((reason, index) => (
                  <div
                    key={index}
                    className="bg-gray-700 rounded p-3"
                  >
                    <div className="font-medium">{reason.reason}</div>
                    <div className="text-sm text-gray-400">{reason.details}</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Most Impactful Filter */}
          {topFilter && (
            <div>
              <h3 className="text-lg font-semibold mb-3">
                Most Impactful Filter
              </h3>
              <div className="bg-gray-700 rounded p-3">
                <div className="font-medium">
                  {topFilter[0].replace('Filter', '')}
                </div>
                <div className="text-sm text-gray-400">
                  Dropped {topFilter[1]} candidate{topFilter[1] !== 1 ? 's' : ''}
                </div>
              </div>
            </div>
          )}

          {/* Full JSON (Collapsed) */}
          <details>
            <summary className="cursor-pointer text-gray-400 hover:text-white font-medium">
              Full Decision JSON
            </summary>
            <pre className="mt-2 p-3 bg-gray-900 rounded text-xs overflow-auto">
              {JSON.stringify(decision, null, 2) as any}
            </pre>
          </details>
        </div>
      </div>
    </div>
  );
}

