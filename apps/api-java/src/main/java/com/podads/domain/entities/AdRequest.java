package com.podads.domain.entities;

import com.podads.domain.valueobjects.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdRequest {
    private String requestId;
    private PodcastContext podcast;
    private SlotContext slot;
    private ListenerContext listener;
    private Instant timestamp;
}


