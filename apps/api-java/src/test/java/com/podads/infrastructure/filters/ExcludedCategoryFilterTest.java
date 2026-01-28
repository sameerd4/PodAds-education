package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import com.podads.domain.valueobjects.PodcastCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcludedCategoryFilterTest {

    private ExcludedCategoryFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ExcludedCategoryFilter();
    }

    @Test
    void testGetName() {
        assertEquals("ExcludedCategoryFilter", filter.getName());
    }

    @Test
    void testPassesWhenCategoryNotExcluded() {
        AdRequest request = TestDataBuilder.adRequest()
                .category(PodcastCategory.FITNESS)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .excludeCategories(List.of(PodcastCategory.NEWS))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testFailsWhenCategoryIsExcluded() {
        AdRequest request = TestDataBuilder.adRequest()
                .category(PodcastCategory.FITNESS)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .excludeCategories(List.of(PodcastCategory.FITNESS))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.EXCLUDED_CATEGORY, result.getReasonCode());
        assertTrue(result.getDetails().contains("FITNESS"));
        assertTrue(result.getDetails().contains("excluded"));
    }

    @Test
    void testFailsWhenCategoryInMultipleExcluded() {
        AdRequest request = TestDataBuilder.adRequest()
                .category(PodcastCategory.TECH)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .excludeCategories(List.of(PodcastCategory.NEWS, PodcastCategory.TECH, PodcastCategory.FINANCE))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.EXCLUDED_CATEGORY, result.getReasonCode());
    }

    @Test
    void testPassesWhenNoExcludedCategories() {
        AdRequest request = TestDataBuilder.adRequest()
                .category(PodcastCategory.FITNESS)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .excludeCategories(List.of())
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenExcludedCategoriesIsNull() {
        AdRequest request = TestDataBuilder.adRequest()
                .category(PodcastCategory.FITNESS)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .excludeCategories(null)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }
}
