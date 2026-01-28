package com.podads.domain.entities;

import com.podads.domain.valueobjects.PodcastCategory;
import com.podads.domain.valueobjects.DeviceType;
import com.podads.domain.valueobjects.TierType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetingRule {
    private List<String> geo;
    private List<DeviceType> device;
    private List<TierType> tier;
    private List<PodcastCategory> categories;
    private List<String> shows;
    private List<PodcastCategory> excludeCategories;
}


