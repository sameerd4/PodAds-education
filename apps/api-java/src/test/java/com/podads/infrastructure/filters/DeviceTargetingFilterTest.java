package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.valueobjects.DeviceType;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeviceTargetingFilterTest {

    private DeviceTargetingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new DeviceTargetingFilter();
    }

    @Test
    void testGetName() {
        assertEquals("DeviceTargetingFilter", filter.getName());
    }

    @Test
    void testPassesWhenDeviceMatches() {
        AdRequest request = TestDataBuilder.adRequest()
                .device(DeviceType.MOBILE)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetDevices(List.of(DeviceType.MOBILE))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenDeviceInMultipleTargets() {
        AdRequest request = TestDataBuilder.adRequest()
                .device(DeviceType.DESKTOP)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetDevices(List.of(DeviceType.MOBILE, DeviceType.DESKTOP, DeviceType.SMART_SPEAKER))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testFailsWhenDeviceDoesNotMatch() {
        AdRequest request = TestDataBuilder.adRequest()
                .device(DeviceType.MOBILE)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetDevices(List.of(DeviceType.DESKTOP))
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertFalse(result.getPassed());
        assertEquals(FilterReasonCode.DEVICE_MISMATCH, result.getReasonCode());
        assertTrue(result.getDetails().contains("MOBILE"));
        assertTrue(result.getDetails().contains("not in targeting list"));
    }

    @Test
    void testPassesWhenNoDeviceTargeting() {
        AdRequest request = TestDataBuilder.adRequest()
                .device(DeviceType.MOBILE)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetDevices(List.of())
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }

    @Test
    void testPassesWhenDeviceTargetingIsNull() {
        AdRequest request = TestDataBuilder.adRequest()
                .device(DeviceType.MOBILE)
                .build();
        CandidateAd candidate = TestDataBuilder.candidateAd()
                .targetDevices(null)
                .build();

        FilterResult result = filter.apply(request, candidate, 12345);

        assertTrue(result.getPassed());
    }
}
