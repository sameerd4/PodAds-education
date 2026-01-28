package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeviceTargetingFilter implements Filter {
    @Override
    public String getName() {
        return "DeviceTargetingFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        List<com.podads.domain.valueobjects.DeviceType> targeting = candidate.getCampaign().getTargeting().getDevice();
        if (targeting == null || targeting.isEmpty()) {
            return FilterResult.builder().passed(true).build();
        }
        if (!targeting.contains(request.getListener().getDevice())) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.DEVICE_MISMATCH)
                    .details("Listener device " + request.getListener().getDevice() + " not in targeting list")
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}


