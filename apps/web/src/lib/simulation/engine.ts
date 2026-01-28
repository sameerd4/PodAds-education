import {
  AdRequest,
  AdDecision,
  PipelineStage,
  ServeInstruction,
  CandidateAd,
} from '@podads/shared';
import { SeededRandom } from './seededRandom';
import { loadCandidates, filterCandidatesByCategory } from './sourcing';
import { allFilters } from './filters';
import { runAuction } from './auction';
import { extractBrandName, normalizeBrandName } from '../brandLogos';

/**
 * Generate a unique decision ID
 */
function generateDecisionId(seed: number): string {
  return `dec-${Date.now()}-${seed}`;
}

/**
 * Generate tracking URLs
 */
function generateTrackingUrls(decisionId: string, _creativeId: string): ServeInstruction['trackingUrls'] {
  const base = `https://tracking.podads.lab/events/${decisionId}`;
  return {
    impression: `${base}/impression`,
    quartiles: [
      `${base}/quartile/25`,
      `${base}/quartile/50`,
      `${base}/quartile/75`,
      `${base}/quartile/100`,
    ],
    complete: `${base}/complete`,
    click: `${base}/click`,
  };
}

/**
 * Run the complete decision pipeline
 */
export function runDecisionPipeline(
  request: AdRequest,
  seed: number
): AdDecision {
  const random = new SeededRandom(seed);
  const decisionId = generateDecisionId(seed);
  const stages: PipelineStage[] = [];

  // Stage 1: Request
  const requestStageStart = performance.now();
  // Request parsing/validation happens here (implicitly)
  const requestLatency = performance.now() - requestStageStart;
  stages.push({
    stageName: 'Request',
    latencyMs: requestLatency,
    inputSummary: `${request.podcast.category} / ${request.slot.type}`,
    outputSummary: `Request received for ${request.podcast.show}`,
    debugPayload: { requestId: request.requestId },
  });

  // Stage 2: Sourcing
  const sourcingStageStart = performance.now();
  let candidates = loadCandidates();
  candidates = filterCandidatesByCategory(candidates, request.podcast.category);
  const sourcingLatency = performance.now() - sourcingStageStart;
  stages.push({
    stageName: 'Sourcing',
    latencyMs: sourcingLatency,
    inputSummary: `Category: ${request.podcast.category}`,
    outputSummary: `Found ${candidates.length} candidate ads`,
    debugPayload: { candidateCount: candidates.length },
  });

  // Stage 3: Filters
  const filterStageStart = performance.now();
  const filterResults = new Map<string, Record<string, { passed: boolean; reasonCode?: string; details?: string }>>();
  const passedCandidates: CandidateAd[] = [];

  for (const candidate of candidates) {
    const candidateId = `${candidate.campaign.id}-${candidate.creative.id}`;
    const results: Record<string, any> = {};

    let allPassed = true;
    for (const filter of allFilters) {
      const result = filter.apply(request, candidate, random);
      results[filter.name] = {
        passed: result.passed,
        reasonCode: result.reasonCode,
        details: result.details,
      };
      if (!result.passed) {
        allPassed = false;
        break; // Short-circuit on first failure
      }
    }

    filterResults.set(candidateId, results);
    if (allPassed) {
      passedCandidates.push(candidate);
    }
  }

  const filterLatency = performance.now() - filterStageStart;
  const dropCount = candidates.length - passedCandidates.length;
  
  // Aggregate filter failures
  const filterFailures: Record<string, number> = {};
  for (const [, results] of filterResults.entries()) {
    for (const [filterName, result] of Object.entries(results)) {
      if (!result.passed) {
        filterFailures[filterName] = (filterFailures[filterName] || 0) + 1;
        break; // Only count the first failure (since we short-circuit)
      }
    }
  }
  
  stages.push({
    stageName: 'Filters',
    latencyMs: filterLatency,
    inputSummary: `${candidates.length} candidates`,
    outputSummary: `${passedCandidates.length} passed, ${dropCount} dropped`,
    debugPayload: {
      totalCandidates: candidates.length,
      passedCount: passedCandidates.length,
      droppedCount: dropCount,
      filterFailures: filterFailures,
    },
  });

  // Stage 4: Auction
  const auctionStageStart = performance.now();
  const scoredCandidates = runAuction(request, passedCandidates, filterResults, random);
  
  // Also create entries for failed candidates so they show up in the UI
  const allCandidatesWithScores: typeof scoredCandidates = [...scoredCandidates];
  for (const candidate of candidates) {
    const candidateId = `${candidate.campaign.id}-${candidate.creative.id}`;
    const filters = filterResults.get(candidateId) || {};
    const passedAllFilters = Object.values(filters).every((f: any) => f.passed);
    
    // If candidate failed filters, add it with a zero score so it shows in UI
    if (!passedAllFilters) {
      // Extract brand name from campaign name
      const campaignName = candidate.campaign.name;
      const brandName = extractBrandName(campaignName) || campaignName.split(' ')[0];
      
      allCandidatesWithScores.push({
        candidateId,
        campaignId: candidate.campaign.id,
        campaignName,
        brandName,
        creativeId: candidate.creative.id,
        filterResults: filters as any,
        score: {
          bidCpm: candidate.campaign.bidCpm,
          matchScore: 0,
          pacingMultiplier: 0,
          finalScore: 0,
          breakdown: {
            categoryMatch: 0,
            showMatch: 0,
            listenerSegmentWeight: 1.0,
          },
        },
        passedAllFilters: false,
      } as any);
    }
  }
  
  // Sort by final score (descending) - failed candidates will be at the bottom
  allCandidatesWithScores.sort((a, b) => b.score.finalScore - a.score.finalScore);
  
  const auctionLatency = performance.now() - auctionStageStart;
  const winner = scoredCandidates.length > 0 ? scoredCandidates[0] : null;

  stages.push({
    stageName: 'Auction',
    latencyMs: auctionLatency,
    inputSummary: `${passedCandidates.length} eligible candidates`,
    outputSummary: winner
      ? `Winner: ${(winner as any).brandName || winner.campaignId} (${winner.campaignId}) - score: ${winner.score.finalScore.toFixed(2)}`
      : 'No winner',
    debugPayload: {
      scoredCount: scoredCandidates.length,
      topScore: winner?.score.finalScore,
    },
  });

  // Stage 5: Serve
  const serveStageStart = performance.now();
  let serveInstruction: ServeInstruction | undefined;
  if (winner) {
    const winningCandidate = passedCandidates.find(
      (c) => c.campaign.id === winner.campaignId && c.creative.id === winner.creativeId
    )!;
    const campaignName = winningCandidate.campaign.name;
    // Extract and normalize to ensure full brand names (e.g., "Capital" -> "Capital One")
    const extractedBrand = extractBrandName(campaignName);
    const brandName = extractedBrand 
      ? normalizeBrandName(extractedBrand, campaignName) || extractedBrand
      : normalizeBrandName(campaignName.split(' ')[0], campaignName) || campaignName.split(' ')[0];
    
    serveInstruction = {
      creativeId: winner.creativeId,
      campaignId: winner.campaignId,
      campaignName,
      brandName,
      assetUrl: winningCandidate.creative.assetUrl,
      durationSeconds: winningCandidate.creative.durationSeconds,
      trackingUrls: generateTrackingUrls(decisionId, winner.creativeId),
      pricePaid: scoredCandidates.length > 1
        ? scoredCandidates[1].score.bidCpm // Second-price auction
        : winner.score.bidCpm,
    } as any;
  }
  const serveLatency = performance.now() - serveStageStart;
  stages.push({
    stageName: 'Serve',
    latencyMs: serveLatency,
    inputSummary: winner ? `Winner: ${(winner as any).brandName || winner.campaignId} (${winner.campaignId})` : 'No winner',
    outputSummary: winner
      ? `Serving ${(serveInstruction as any).brandName} creative ${serveInstruction!.creativeId}`
      : 'No fill',
    debugPayload: {
      served: !!winner,
      pricePaid: serveInstruction?.pricePaid,
    },
  });

  return {
    decisionId,
    requestId: request.requestId,
    seed,
    timestamp: new Date().toISOString(),
    stages,
    candidates: allCandidatesWithScores, // Include both passed and failed candidates
    winner: winner && serveInstruction
      ? {
          candidate: winner,
          serve: serveInstruction,
        }
      : undefined,
    noFillReason: winner ? undefined : 'No eligible candidates after filtering',
  };
}

