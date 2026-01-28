import { FilterResult } from './FilterResult';

export interface AuctionScore {
  bidCpm: number; // in cents
  matchScore: number; // 0.0 to 1.0
  pacingMultiplier: number; // 0.0 to 1.0 (lower when pacing is tight)
  finalScore: number; // bidCpm * matchScore * pacingMultiplier
  breakdown: {
    categoryMatch: number; // 0.0 to 1.0
    showMatch: number; // 0.0 to 1.0
    listenerSegmentWeight?: number; // Optional listener targeting boost
  };
}

export interface CandidateWithScore {
  candidateId: string;
  campaignId: string;
  creativeId: string;
  filterResults: Record<string, FilterResult>; // filterName -> FilterResult
  score: AuctionScore;
  passedAllFilters: boolean;
}


