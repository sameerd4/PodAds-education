import {
  AdRequest,
  CandidateAd,
  FilterResult,
} from '@podads/shared';
import { SeededRandom } from './seededRandom';

export interface Filter {
  name: string;
  apply: (
    request: AdRequest,
    candidate: CandidateAd,
    random: SeededRandom
  ) => FilterResult;
}

/**
 * Campaign Status Filter
 * Checks if campaign is active
 */
export const campaignStatusFilter: Filter = {
  name: 'CampaignStatusFilter',
  apply: (_request, candidate) => {
    if (candidate.campaign.status !== 'active') {
      return {
        passed: false,
        reasonCode: 'campaign_inactive',
        details: `Campaign status is ${candidate.campaign.status}`,
      };
    }
    return { passed: true };
  },
};

/**
 * Schedule Window Filter
 * Checks if current time is within campaign start/end dates
 */
export const scheduleWindowFilter: Filter = {
  name: 'ScheduleWindowFilter',
  apply: (request, candidate) => {
    const now = new Date(request.timestamp);
    const start = new Date(candidate.campaign.startDate);
    const end = new Date(candidate.campaign.endDate);

    if (now < start) {
      return {
        passed: false,
        reasonCode: 'outside_schedule_window',
        details: `Campaign starts on ${start.toISOString()}`,
      };
    }
    if (now > end) {
      return {
        passed: false,
        reasonCode: 'outside_schedule_window',
        details: `Campaign ended on ${end.toISOString()}`,
      };
    }
    return { passed: true };
  },
};

/**
 * Geo Targeting Filter
 */
export const geoTargetingFilter: Filter = {
  name: 'GeoTargetingFilter',
  apply: (request, candidate) => {
    const targeting = candidate.campaign.targeting.geo;
    if (!targeting || targeting.length === 0) {
      return { passed: true }; // No geo targeting = allow all
    }
    if (!targeting.includes(request.listener.geo)) {
      return {
        passed: false,
        reasonCode: 'geo_mismatch',
        details: `Listener geo ${request.listener.geo} not in targeting list`,
      };
    }
    return { passed: true };
  },
};

/**
 * Device Targeting Filter
 */
export const deviceTargetingFilter: Filter = {
  name: 'DeviceTargetingFilter',
  apply: (request, candidate) => {
    const targeting = candidate.campaign.targeting.device;
    if (!targeting || targeting.length === 0) {
      return { passed: true };
    }
    if (!targeting.includes(request.listener.device)) {
      return {
        passed: false,
        reasonCode: 'device_mismatch',
        details: `Listener device ${request.listener.device} not in targeting list`,
      };
    }
    return { passed: true };
  },
};

/**
 * Tier Targeting Filter
 */
export const tierTargetingFilter: Filter = {
  name: 'TierTargetingFilter',
  apply: (request, candidate) => {
    const targeting = candidate.campaign.targeting.tier;
    if (!targeting || targeting.length === 0) {
      return { passed: true };
    }
    if (!targeting.includes(request.listener.tier)) {
      return {
        passed: false,
        reasonCode: 'tier_mismatch',
        details: `Listener tier ${request.listener.tier} not in targeting list`,
      };
    }
    return { passed: true };
  },
};

/**
 * Category Match Filter
 */
export const categoryMatchFilter: Filter = {
  name: 'CategoryMatchFilter',
  apply: (request, candidate) => {
    const targeting = candidate.campaign.targeting.categories;
    if (!targeting || targeting.length === 0) {
      return { passed: true };
    }
    if (!targeting.includes(request.podcast.category)) {
      return {
        passed: false,
        reasonCode: 'category_mismatch',
        details: `Podcast category ${request.podcast.category} not in targeting list`,
      };
    }
    return { passed: true };
  },
};

/**
 * Excluded Category Filter
 */
export const excludedCategoryFilter: Filter = {
  name: 'ExcludedCategoryFilter',
  apply: (request, candidate) => {
    const excluded = candidate.campaign.targeting.excludeCategories;
    if (!excluded || excluded.length === 0) {
      return { passed: true };
    }
    if (excluded.includes(request.podcast.category)) {
      return {
        passed: false,
        reasonCode: 'excluded_category',
        details: `Podcast category ${request.podcast.category} is excluded`,
      };
    }
    return { passed: true };
  },
};

/**
 * Slot Type Filter
 */
export const slotTypeFilter: Filter = {
  name: 'SlotTypeFilter',
  apply: (request, candidate) => {
    if (!candidate.eligibleSlotTypes.includes(request.slot.type)) {
      return {
        passed: false,
        reasonCode: 'slot_type_mismatch',
        details: `Slot type ${request.slot.type} not eligible for this creative`,
      };
    }
    return { passed: true };
  },
};

/**
 * Creative Approval Filter
 */
export const creativeApprovalFilter: Filter = {
  name: 'CreativeApprovalFilter',
  apply: (_request, candidate) => {
    if (candidate.creative.approvalStatus !== 'approved') {
      return {
        passed: false,
        reasonCode: 'creative_not_approved',
        details: `Creative status is ${candidate.creative.approvalStatus}`,
      };
    }
    return { passed: true };
  },
};

/**
 * Budget Remaining Filter
 * Simulates budget check (in real system, this would check Redis/DB)
 */
export const budgetRemainingFilter: Filter = {
  name: 'BudgetRemainingFilter',
  apply: (_request, candidate, random) => {
    // Simulate: 90% chance budget is available if remaining > 0
    const hasBudget = candidate.campaign.budget.remaining > 0;
    if (!hasBudget) {
      return {
        passed: false,
        reasonCode: 'budget_exhausted',
        details: 'Campaign budget exhausted',
      };
    }
    // Simulate occasional budget exhaustion even with remaining > 0
    // (e.g., concurrent reservations) - reduced to 1% chance
    if (random.random() < 0.01 && candidate.campaign.budget.remaining < 10000) {
      return {
        passed: false,
        reasonCode: 'budget_exhausted',
        details: 'Budget exhausted due to concurrent reservations',
      };
    }
    return { passed: true };
  },
};

/**
 * Pacing Gate Filter
 * Simulates daily pacing limits
 */
export const pacingGateFilter: Filter = {
  name: 'PacingGateFilter',
  apply: (_request, candidate, random) => {
    const pacing = candidate.campaign.pacing;
    if (!pacing.dailyBudget) {
      return { passed: true }; // No pacing limit
    }
    // Simulate: if daily spend is close to daily budget, throttle
    const spendRatio = pacing.dailySpend! / pacing.dailyBudget;
    if (spendRatio >= 1.0) {
      return {
        passed: false,
        reasonCode: 'pacing_limit_exceeded',
        details: 'Daily pacing limit exceeded',
      };
    }
    // Simulate throttling as we approach limit - reduced to 10% chance
    if (spendRatio > 0.9 && random.random() < 0.1) {
      return {
        passed: false,
        reasonCode: 'pacing_limit_exceeded',
        details: 'Pacing throttled to stay within daily budget',
      };
    }
    return { passed: true };
  },
};

/**
 * Frequency Cap Filter
 * Simulates frequency capping
 */
export const frequencyCapFilter: Filter = {
  name: 'FrequencyCapFilter',
  apply: (_request, candidate, random) => {
    const freqCap = candidate.campaign.frequencyCap;
    if (!freqCap) {
      return { passed: true }; // No frequency cap
    }
    // Simulate: 1% chance of hitting frequency cap (reduced from 10%)
    if (random.random() < 0.01) {
      return {
        passed: false,
        reasonCode: 'frequency_cap_exceeded',
        details: `Frequency cap exceeded: ${freqCap.maxImpressions} impressions in ${freqCap.windowHours}h`,
      };
    }
    return { passed: true };
  },
};

/**
 * All filters in order
 */
export const allFilters: Filter[] = [
  campaignStatusFilter,
  scheduleWindowFilter,
  slotTypeFilter,
  creativeApprovalFilter,
  geoTargetingFilter,
  deviceTargetingFilter,
  tierTargetingFilter,
  categoryMatchFilter,
  excludedCategoryFilter,
  budgetRemainingFilter,
  pacingGateFilter,
  frequencyCapFilter,
];

