package com.podads.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PodcastContext {
    private PodcastCategory category;
    private String show;
    private String episode;
}


