package com.podads.domain.entities;

import com.podads.domain.valueobjects.PodcastCategory;
import com.podads.domain.valueobjects.DeviceType;
import com.podads.domain.valueobjects.TierType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {
    private String id;
    private String advertiserId;
    private String name;
    private CampaignStatus status;
    private Budget budget;
    private Integer bidCpm; // in cents
    private Instant startDate;
    private Instant endDate;
    private TargetingRule targeting;
    private Pacing pacing;
    private FrequencyCap frequencyCap;

    public enum CampaignStatus {
        ACTIVE, PAUSED, ENDED, DRAFT
    }
}


