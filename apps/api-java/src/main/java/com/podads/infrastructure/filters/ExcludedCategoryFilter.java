package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExcludedCategoryFilter implements Filter {
    @Override
    public String getName() {
        return "ExcludedCategoryFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        List<com.podads.domain.valueobjects.PodcastCategory> excluded = candidate.getCampaign().getTargeting().getExcludeCategories();
        if (excluded == null || excluded.isEmpty()) {
            return FilterResult.builder().passed(true).build();
        }
        if (excluded.contains(request.getPodcast().getCategory())) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.EXCLUDED_CATEGORY)
                    .details("Podcast category " + request.getPodcast().getCategory() + " is excluded")
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}


