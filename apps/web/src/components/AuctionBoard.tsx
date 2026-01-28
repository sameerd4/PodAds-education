import { AdDecision, FilterResult } from '@podads/shared';
import BrandLogo from './BrandLogo';

interface AuctionBoardProps {
  decision: AdDecision | null;
}

// Human-readable filter names
const FILTER_NAMES: Record<string, string> = {
  CampaignStatusFilter: 'Campaign Status',
  ScheduleWindowFilter: 'Schedule Window',
  GeoTargetingFilter: 'Geo Targeting',
  DeviceTargetingFilter: 'Device Targeting',
  TierTargetingFilter: 'Tier Targeting',
  CategoryMatchFilter: 'Category Match',
  ExcludedCategoryFilter: 'Excluded Category',
  SlotTypeFilter: 'Slot Type',
  CreativeApprovalFilter: 'Creative Approval',
  BudgetRemainingFilter: 'Budget Remaining',
  PacingGateFilter: 'Pacing Gate',
  FrequencyCapFilter: 'Frequency Cap',
};

// Human-readable reason explanations
const REASON_EXPLANATIONS: Record<string, string> = {
  campaign_inactive: 'Campaign is not active',
  campaign_ended: 'Campaign has ended',
  outside_schedule_window: 'Outside scheduled time window',
  geo_mismatch: 'Location does not match targeting',
  device_mismatch: 'Device type does not match targeting',
  tier_mismatch: 'Subscription tier does not match targeting',
  category_mismatch: 'Podcast category does not match targeting',
  excluded_category: 'Podcast category is excluded',
  slot_type_mismatch: 'Ad slot type not eligible',
  creative_not_approved: 'Creative not approved',
  budget_exhausted: 'Campaign budget exhausted',
  pacing_limit_exceeded: 'Daily spending limit reached',
  frequency_cap_exceeded: 'Too many impressions to this listener',
};

export default function AuctionBoard({ decision }: AuctionBoardProps) {
  if (!decision) {
    return (
      <div className="bg-gray-800 rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-4">Auction Board</h2>
        <p className="text-gray-400">Run a decision to see candidates</p>
      </div>
    );
  }

  const candidates = decision.candidates.slice(0, 10); // Show top 10

  return (
    <div className="bg-gray-800 rounded-lg p-6">
      <h2 className="text-xl font-semibold mb-4">Auction Board</h2>
      {candidates.length === 0 ? (
        <p className="text-gray-400">No eligible candidates</p>
      ) : (
        <div className="space-y-2">
          {candidates.map((candidate: typeof decision.candidates[0], index: number) => {
            const isWinner = decision.winner?.candidate.candidateId === candidate.candidateId;
            const filterFailures = Object.entries(candidate.filterResults).filter(
              ([_, result]: [string, any]) => !result.passed
            );

            return (
              <div
                key={candidate.candidateId}
                className={`border rounded-lg p-3 ${
                  isWinner
                    ? 'border-green-500 bg-green-900/20'
                    : 'border-gray-700 bg-gray-700/50'
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-3">
                    <BrandLogo
                      brandName={(candidate as any).brandName}
                      campaignName={(candidate as any).campaignName}
                      size={40}
                    />
                    <div className="flex items-center gap-2">
                      {isWinner && (
                        <span className="text-xs font-bold text-green-400">WINNER</span>
                      )}
                      <div className="flex flex-col">
                        <span className="text-base font-bold text-white">
                          {(candidate as any).brandName || candidate.campaignId}
                        </span>
                        {(candidate as any).campaignName && (
                          <span className="text-xs text-gray-400">
                            {(candidate as any).campaignName}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                  <span className="text-xs text-gray-400">#{index + 1}</span>
                </div>

                <div className="grid grid-cols-2 gap-2 text-xs mb-2">
                  <div>
                    <span className="text-gray-400">Bid:</span>{' '}
                    <span className="text-white">${(candidate.score.bidCpm / 100).toFixed(2)}</span>
                  </div>
                  <div>
                    <span className="text-gray-400">Match:</span>{' '}
                    <span className="text-white">
                      {(candidate.score.matchScore * 100).toFixed(0)}%
                    </span>
                  </div>
                  <div>
                    <span className="text-gray-400">Pacing:</span>{' '}
                    <span className="text-white">
                      {(candidate.score.pacingMultiplier * 100).toFixed(0)}%
                    </span>
                  </div>
                  <div>
                    <span className="text-gray-400">Final:</span>{' '}
                    <span className="text-white font-bold">
                      ${(candidate.score.finalScore / 100).toFixed(2)}
                    </span>
                  </div>
                </div>

                {filterFailures.length > 0 && (
                  <div className="mt-3 pt-3 border-t border-red-500/30">
                    <div className="text-xs font-semibold text-red-400 mb-2">
                      Filtered Out
                    </div>
                    <div className="space-y-1.5">
                      {filterFailures.map(([filterName, result]: [string, FilterResult], idx) => {
                        const displayName = FILTER_NAMES[filterName] || filterName.replace('Filter', '');
                        const reason = result.reasonCode 
                          ? REASON_EXPLANATIONS[result.reasonCode] || result.reasonCode 
                          : 'Unknown reason';
                        
                        return (
                          <div key={idx} className="bg-red-900/20 border border-red-500/30 rounded p-2 text-xs">
                            <div className="text-red-300 font-medium mb-0.5">
                              {displayName}
                            </div>
                            <div className="text-gray-300">{reason}</div>
                            {result.details && (
                              <div className="text-gray-400 text-[10px] mt-1 italic">
                                {result.details}
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

