import { CandidateAd, PodcastCategory, SlotType } from '@podads/shared';
import campaignsData from '../../fixtures/campaigns.json';
import creativesData from '../../fixtures/creatives.json';

/**
 * Load all campaigns and creatives from fixtures
 */
export function loadCandidates(): CandidateAd[] {
  const candidates: CandidateAd[] = [];

  for (const campaign of campaignsData.campaigns) {
    // Find creatives for this campaign
    const campaignCreatives = creativesData.creatives.filter(
      (c) => c.campaignId === campaign.id
    );

    for (const creative of campaignCreatives) {
      candidates.push({
        campaign: campaign as any,
        creative: creative as any,
        eligibleSlotTypes: (creative.eligibleSlotTypes || ['pre-roll', 'mid-roll', 'post-roll']) as SlotType[],
      });
    }
  }

  return candidates;
}

/**
 * Filter candidates by category (for scenario-based sourcing)
 */
export function filterCandidatesByCategory(
  candidates: CandidateAd[],
  category: PodcastCategory
): CandidateAd[] {
  return candidates.filter((c) => {
    const categories = c.campaign.targeting.categories;
    if (!categories || categories.length === 0) {
      return true; // No category targeting = include
    }
    return categories.includes(category);
  });
}

