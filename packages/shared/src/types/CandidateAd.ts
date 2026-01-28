import { SlotType, PodcastCategory } from './AdRequest';

export interface Creative {
  id: string;
  campaignId: string;
  durationSeconds: number;
  assetUrl: string;
  approvalStatus: 'approved' | 'pending' | 'rejected';
}

export interface TargetingRule {
  geo?: string[]; // ISO country codes
  device?: ('mobile' | 'desktop' | 'smart-speaker' | 'car')[];
  tier?: ('free' | 'premium')[];
  categories?: PodcastCategory[];
  shows?: string[];
  excludeCategories?: PodcastCategory[];
}

export interface Campaign {
  id: string;
  advertiserId: string;
  name: string;
  status: 'active' | 'paused' | 'ended' | 'draft';
  budget: {
    total: number; // in cents
    remaining: number; // in cents
  };
  bidCpm: number; // cost per mille (thousand impressions) in cents
  startDate: string; // ISO 8601
  endDate: string; // ISO 8601
  targeting: TargetingRule;
  pacing: {
    dailyBudget?: number; // in cents
    dailySpend?: number; // in cents
  };
  frequencyCap?: {
    maxImpressions: number;
    windowHours: number;
  };
}

export interface CandidateAd {
  campaign: Campaign;
  creative: Creative;
  eligibleSlotTypes: SlotType[];
}


