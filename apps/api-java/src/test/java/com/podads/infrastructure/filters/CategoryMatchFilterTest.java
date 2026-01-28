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

class CategoryMatchFilterTest {

    private CategoryMatchFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CategoryMatchFilter();
    }

    @Test
    void testGetName() {
        assertEquals("CategoryMatchFilter", filter.getName());
    }

    @Test
    void testPassesWhenCategoryMatches() {
        AdRequest request = TestDataBuilder.adRequest()
                .category(PodcastCategory.FITNESS)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetCategories(List.of(PodcastCategory.FITNESS))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenCategoryInMultipleTargets() {
        AdRequest request = TestDataBuilder.adRequest()
                .category(PodcastCategory.TECH)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetCategories(List.of(PodcastCategory.FITNESS, PodcastCategory.TECH, PodcastCategory.SPORTS))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testFailsWhenCategoryDoesNotMatch() {
        AdRequest request = TestDataBuilder.adRequest()
                .category(PodcastCategory.FITNESS)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetCategories(List.of(PodcastCategory.TECH))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.CATEGORY_MISMATCH, result.getReasonCode());
        assertTrue(result.getDetails().contains("FITNESS"));
        assertTrue(result.getDetails().contains("not in targeting list"));
    }

    @Test
    void testPassesWhenNoTargetingCategories() {
        AdRequest request = TestDataBuilder.adRequest()
                .category(PodcastCategory.FITNESS)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetCategories(List.of())
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenTargetingCategoriesIsNull() {
        AdRequest request = TestDataBuilder.adRequest()
                .category(PodcastCategory.FITNESS)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetCategories(null)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }
}
