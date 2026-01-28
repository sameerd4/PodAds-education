package com.podads.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Creative {
    private String id;
    private String campaignId;
    private Integer durationSeconds;
    private String assetUrl;
    private ApprovalStatus approvalStatus;

    public enum ApprovalStatus {
        APPROVED, PENDING, REJECTED
    }
}


