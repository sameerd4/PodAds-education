import {
  AdRequest,
  CandidateAd,
  AuctionScore,
  CandidateWithScore,
} from '@podads/shared';
import { SeededRandom } from './seededRandom';
import { extractBrandName, normalizeBrandName } from '../brandLogos';

/**
 * Calculate match score based on category/show targeting
 */
function calculateMatchScore(
  request: AdRequest,
  candidate: CandidateAd
): { categoryMatch: number; showMatch: number; listenerSegmentWeight: number } {
  let categoryMatch = 0.5; // Default
  let showMatch = 0.5; // Default
  let listenerSegmentWeight = 1.0;

  // Category match
  const categories = candidate.campaign.targeting.categories;
  if (categories && categories.includes(request.podcast.category)) {
    categoryMatch = 1.0;
  } else if (categories && categories.length > 0) {
    categoryMatch = 0.3; // Partial match if targeting exists but doesn't match
  }

  // Show match (higher weight if specific show is targeted)
  const shows = candidate.campaign.targeting.shows;
  if (shows && shows.includes(request.podcast.show)) {
    showMatch = 1.0;
  } else if (shows && shows.length > 0) {
    showMatch = 0.4;
  }

  // Listener segment weight (boost for premium users, specific devices, etc.)
  if (request.listener.tier === 'premium') {
    listenerSegmentWeight = 1.1;
  }
  if (request.listener.device === 'smart-speaker') {
    listenerSegmentWeight *= 1.05;
  }

  return { categoryMatch, showMatch, listenerSegmentWeight };
}

/**
 * Calculate pacing multiplier (reduces score when pacing is tight)
 */
function calculatePacingMultiplier(candidate: CandidateAd): number {
  const pacing = candidate.campaign.pacing;
  if (!pacing.dailyBudget) {
    return 1.0; // No pacing = no multiplier
  }
  const spendRatio = pacing.dailySpend! / pacing.dailyBudget;
  if (spendRatio >= 1.0) {
    return 0.0; // Completely throttled
  }
  if (spendRatio > 0.9) {
    return 0.3; // Heavily throttled
  }
  if (spendRatio > 0.7) {
    return 0.7; // Moderately throttled
  }
  return 1.0; // No throttling
}

/**
 * Score a candidate for auction
 */
export function scoreCandidate(
  request: AdRequest,
  candidate: CandidateAd,
  _random: SeededRandom
): AuctionScore {
  const bidCpm = candidate.campaign.bidCpm;
  const { categoryMatch, showMatch, listenerSegmentWeight } =
    calculateMatchScore(request, candidate);
  const pacingMultiplier = calculatePacingMultiplier(candidate);

  // Combined match score (weighted average)
  const matchScore =
    categoryMatch * 0.6 + showMatch * 0.4 * listenerSegmentWeight;

  // Final score = bid × match × pacing
  const finalScore = bidCpm * matchScore * pacingMultiplier;

  return {
    bidCpm,
    matchScore,
    pacingMultiplier,
    finalScore,
    breakdown: {
      categoryMatch,
      showMatch,
      listenerSegmentWeight,
    },
  };
}

/**
 * Run auction on candidates that passed all filters
 */
export function runAuction(
  request: AdRequest,
  candidates: CandidateAd[],
  filterResults: Map<string, Record<string, { passed: boolean; reasonCode?: string }>>,
  random: SeededRandom
): CandidateWithScore[] {
  const scored: CandidateWithScore[] = [];

  for (const candidate of candidates) {
    const candidateId = `${candidate.campaign.id}-${candidate.creative.id}`;
    const filters = filterResults.get(candidateId) || {};

    // Check if passed all filters
    const passedAllFilters = Object.values(filters).every((f) => f.passed);

    if (passedAllFilters) {
      const score = scoreCandidate(request, candidate, random);
      // Extract brand name from campaign name (e.g., "Nike Air Max - Just Do It" -> "Nike")
      const campaignName = candidate.campaign.name;
      // Extract and normalize to ensure full brand names (e.g., "Capital" -> "Capital One")
      const extractedBrand = extractBrandName(campaignName);
      const brandName = extractedBrand 
        ? normalizeBrandName(extractedBrand, campaignName) || extractedBrand
        : normalizeBrandName(campaignName.split(' ')[0], campaignName) || campaignName.split(' ')[0];
      
      scored.push({
        candidateId,
        campaignId: candidate.campaign.id,
        campaignName,
        brandName,
        creativeId: candidate.creative.id,
        filterResults: filters as any,
        score,
        passedAllFilters: true,
      } as any);
    }
  }

  // Sort by final score (descending)
  scored.sort((a, b) => b.score.finalScore - a.score.finalScore);

  return scored;
}

