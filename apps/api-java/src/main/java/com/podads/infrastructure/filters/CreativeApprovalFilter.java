package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.entities.Creative;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

@Component
public class CreativeApprovalFilter implements Filter {
    @Override
    public String getName() {
        return "CreativeApprovalFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        if (candidate.getCreative().getApprovalStatus() != Creative.ApprovalStatus.APPROVED) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.CREATIVE_NOT_APPROVED)
                    .details("Creative status is " + candidate.getCreative().getApprovalStatus())
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}

