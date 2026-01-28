package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import com.podads.domain.valueobjects.SlotType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlotTypeFilterTest {

    private SlotTypeFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SlotTypeFilter();
    }

    @Test
    void testGetName() {
        assertEquals("SlotTypeFilter", filter.getName());
    }

    @Test
    void testPassesWhenSlotTypeMatches() {
        AdRequest request = TestDataBuilder.adRequest()
                .slotType(SlotType.MID_ROLL)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .eligibleSlotTypes(List.of(SlotType.MID_ROLL))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenSlotTypeInMultipleEligibleTypes() {
        AdRequest request = TestDataBuilder.adRequest()
                .slotType(SlotType.PRE_ROLL)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .eligibleSlotTypes(List.of(SlotType.PRE_ROLL, SlotType.MID_ROLL, SlotType.POST_ROLL))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testFailsWhenSlotTypeDoesNotMatch() {
        AdRequest request = TestDataBuilder.adRequest()
                .slotType(SlotType.MID_ROLL)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .eligibleSlotTypes(List.of(SlotType.PRE_ROLL))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.SLOT_TYPE_MISMATCH, result.getReasonCode());
    }

    @Test
    void testFailsWhenEligibleSlotTypesIsEmpty() {
        AdRequest request = TestDataBuilder.adRequest()
                .slotType(SlotType.MID_ROLL)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .eligibleSlotTypes(List.of())
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.SLOT_TYPE_MISMATCH, result.getReasonCode());
    }
}
