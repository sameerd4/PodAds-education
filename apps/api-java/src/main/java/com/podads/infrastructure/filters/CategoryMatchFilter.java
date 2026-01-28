package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMatchFilter implements Filter {
    @Override
    public String getName() {
        return "CategoryMatchFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        List<com.podads.domain.valueobjects.PodcastCategory> targeting = candidate.getCampaign().getTargeting().getCategories();
        if (targeting == null || targeting.isEmpty()) {
            return FilterResult.builder().passed(true).build();
        }
        if (!targeting.contains(request.getPodcast().getCategory())) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.CATEGORY_MISMATCH)
                    .details("Podcast category " + request.getPodcast().getCategory() + " not in targeting list")
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}


