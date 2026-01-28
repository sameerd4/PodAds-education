package com.podads.infrastructure.filters;

import com.podads.domain.entities.AdRequest;
import com.podads.domain.entities.CandidateAd;
import com.podads.domain.services.Filter;
import com.podads.domain.valueobjects.FilterReasonCode;
import com.podads.domain.valueobjects.FilterResult;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ScheduleWindowFilter implements Filter {
    @Override
    public String getName() {
        return "ScheduleWindowFilter";
    }

    @Override
    public FilterResult apply(AdRequest request, CandidateAd candidate, int randomSeed) {
        Instant now = request.getTimestamp();
        Instant start = candidate.getCampaign().getStartDate();
        Instant end = candidate.getCampaign().getEndDate();

        if (now.isBefore(start)) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.OUTSIDE_SCHEDULE_WINDOW)
                    .details("Campaign starts on " + start.toString())
                    .build();
        }
        if (now.isAfter(end)) {
            return FilterResult.builder()
                    .passed(false)
                    .reasonCode(FilterReasonCode.OUTSIDE_SCHEDULE_WINDOW)
                    .details("Campaign ended on " + end.toString())
                    .build();
        }
        return FilterResult.builder().passed(true).build();
    }
}


