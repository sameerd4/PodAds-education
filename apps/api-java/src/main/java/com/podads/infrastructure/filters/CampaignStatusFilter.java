package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.entities.Campaign;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

@Component
public class CampaignStatusFilter implements Filter {
    @Override
    public String getName() {
        return "CampaignStatusFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        if (candidate.getCampaign().getStatus() != Campaign.CampaignStatus.ACTIVE) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.CAMPAIGN_INACTIVE)
                    .details("Campaign status is " + candidate.getCampaign().getStatus())
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}

