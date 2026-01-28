import { useState } from 'react';
import { AdDecision, FilterResult } from '@podads/shared';
import BrandLogo from './BrandLogo';

interface PipelineStepperProps {
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

function FilterBreakdown({ decision, filterStage }: { decision: AdDecision; filterStage: typeof decision.stages[0] }) {
  const passedCandidates = decision.candidates.filter(c => c.passedAllFilters);
  const failedCandidates = decision.candidates.filter(c => !c.passedAllFilters);

  return (
    <div className="mt-4 space-y-4">
      {/* Passed Candidates */}
      {passedCandidates.length > 0 && (
        <div>
          <div className="flex items-center gap-2 mb-2">
            <div className="w-2 h-2 rounded-full bg-green-500"></div>
            <h4 className="font-semibold text-green-400">
              Proceeding to Auction ({passedCandidates.length})
            </h4>
          </div>
          <div className="space-y-2">
            {passedCandidates.map((candidate) => (
              <div
                key={candidate.candidateId}
                className="bg-green-900/20 border border-green-500/30 rounded p-2 text-xs flex items-center gap-2"
              >
                <BrandLogo
                  brandName={(candidate as any).brandName}
                  campaignName={(candidate as any).campaignName}
                  size={28}
                />
                <div className="flex-1">
                  <div className="font-medium text-white">
                    {(candidate as any).brandName || candidate.campaignId}
                  </div>
                  {(candidate as any).campaignName && (
                    <div className="text-gray-400 text-[10px] mt-0.5">
                      {(candidate as any).campaignName}
                    </div>
                  )}
                  <div className="text-green-300 mt-1">✓ Passed all filters</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Failed Candidates */}
      {failedCandidates.length > 0 && (
        <div>
          <div className="flex items-center gap-2 mb-2">
            <div className="w-2 h-2 rounded-full bg-red-500"></div>
            <h4 className="font-semibold text-red-400">
              Filtered Out ({failedCandidates.length})
            </h4>
          </div>
          <div className="space-y-2">
            {failedCandidates.map((candidate) => {
              const filterFailures = Object.entries(candidate.filterResults).filter(
                ([_, result]: [string, FilterResult]) => !result.passed
              );
              const firstFailure = filterFailures[0];
              const filterName = firstFailure?.[0] || 'Unknown';
              const filterResult = firstFailure?.[1] as FilterResult;
              
              return (
                <div
                  key={candidate.candidateId}
                  className="bg-red-900/20 border border-red-500/30 rounded p-2 text-xs flex items-start gap-2"
                >
                  <BrandLogo
                    brandName={(candidate as any).brandName}
                    campaignName={(candidate as any).campaignName}
                    size={28}
                  />
                  <div className="flex-1">
                    <div className="font-medium text-white">
                      {(candidate as any).brandName || candidate.campaignId}
                    </div>
                    {(candidate as any).campaignName && (
                      <div className="text-gray-400 text-[10px] mt-0.5">
                        {(candidate as any).campaignName}
                      </div>
                    )}
                    <div className="mt-2 pt-2 border-t border-red-500/20">
                      <div className="text-red-300 font-medium">
                        ✗ {FILTER_NAMES[filterName] || filterName.replace('Filter', '')}
                      </div>
                      {filterResult?.reasonCode && (
                        <div className="text-gray-300 mt-0.5">
                          {REASON_EXPLANATIONS[filterResult.reasonCode] || filterResult.reasonCode}
                        </div>
                      )}
                      {filterResult?.details && (
                        <div className="text-gray-400 text-[10px] mt-1 italic">
                          {filterResult.details}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Filter Summary Stats */}
      {filterStage.debugPayload && (filterStage.debugPayload as any).filterFailures && (
        <div className="mt-4 pt-4 border-t border-gray-600">
          <div className="text-xs font-semibold text-gray-300 mb-2">Filter Failure Summary</div>
          <div className="space-y-1">
            {Object.entries((filterStage.debugPayload as any).filterFailures).map(([filterName, count]: [string, any]) => (
              <div key={filterName} className="flex items-center justify-between text-xs">
                <span className="text-gray-400">{FILTER_NAMES[filterName] || filterName.replace('Filter', '')}</span>
                <span className="text-red-400 font-medium">{count} failed</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default function PipelineStepper({ decision }: PipelineStepperProps) {
  const [expandedStage, setExpandedStage] = useState<string | null>(null);

  if (!decision) {
    return (
      <div className="bg-gray-800 rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-4">Pipeline</h2>
        <p className="text-gray-400">Run a decision to see the pipeline</p>
      </div>
    );
  }

  return (
    <div className="bg-gray-800 rounded-lg p-6">
      <h2 className="text-xl font-semibold mb-4">5-Step Pipeline</h2>
      <div className="space-y-3">
        {decision.stages.map((stage: typeof decision.stages[0], index: number) => (
          <div
            key={stage.stageName}
            className="border border-gray-700 rounded-lg overflow-hidden"
          >
            <button
              onClick={() =>
                setExpandedStage(
                  expandedStage === stage.stageName ? null : stage.stageName
                )
              }
              className="w-full px-4 py-3 flex items-center justify-between hover:bg-gray-700 transition-colors"
            >
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-sm font-bold">
                  {index + 1}
                </div>
                <div className="text-left">
                  <div className="font-medium">{stage.stageName}</div>
                  <div className="text-sm text-gray-400">{stage.outputSummary}</div>
                </div>
              </div>
              <div className="text-sm text-gray-400">
                {stage.latencyMs.toFixed(1)}ms
              </div>
            </button>
            {expandedStage === stage.stageName && (
              <div className="px-4 pb-4 border-t border-gray-700 bg-gray-700/50 animate-fadeIn">
                <div className="mt-3 space-y-2 text-sm">
                  <div>
                    <span className="text-gray-400">Input:</span>{' '}
                    <span className="text-white">{stage.inputSummary}</span>
                  </div>
                  <div>
                    <span className="text-gray-400">Output:</span>{' '}
                    <span className="text-white">{stage.outputSummary}</span>
                  </div>
                  
                  {/* Enhanced Filter Breakdown for Filters stage */}
                  {stage.stageName === 'Filters' && decision && (
                    <FilterBreakdown decision={decision} filterStage={stage} />
                  )}
                  
                  {stage.debugPayload && stage.stageName !== 'Filters' && (
                    <details className="mt-2">
                      <summary className="cursor-pointer text-gray-400 hover:text-white">
                        Debug Payload
                      </summary>
                      <pre className="mt-2 p-2 bg-gray-900 rounded text-xs overflow-auto">
                        {JSON.stringify(stage.debugPayload, null, 2)}
                      </pre>
                    </details>
                  )}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
      <div className="mt-4 pt-4 border-t border-gray-700">
        <div className="text-sm text-gray-400">
          Total Latency:{' '}
          <span className="text-white font-medium">
            {decision.stages
              .reduce((sum: number, s: typeof decision.stages[0]) => sum + s.latencyMs, 0)
              .toFixed(1)}
            ms
          </span>
        </div>
      </div>
    </div>
  );
}

