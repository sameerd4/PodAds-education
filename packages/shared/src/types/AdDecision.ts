import { AdRequest } from './AdRequest';
import { CandidateAd } from './CandidateAd';
import { CandidateWithScore } from './AuctionScore';

export interface PipelineStage {
  stageName: string;
  latencyMs: number;
  inputSummary: string;
  outputSummary: string;
  debugPayload?: Record<string, unknown>;
}

export interface ServeInstruction {
  creativeId: string;
  campaignId: string;
  assetUrl: string;
  durationSeconds: number;
  trackingUrls: {
    impression: string;
    quartiles: string[]; // [25%, 50%, 75%, 100%]
    complete: string;
    click: string;
  };
  pricePaid?: number; // in cents (second-price auction)
}

export interface AdDecision {
  decisionId: string;
  requestId: string;
  seed: number;
  timestamp: string; // ISO 8601
  stages: PipelineStage[];
  candidates: CandidateWithScore[];
  winner?: {
    candidate: CandidateWithScore;
    serve: ServeInstruction;
  };
  noFillReason?: string; // If no winner
}


