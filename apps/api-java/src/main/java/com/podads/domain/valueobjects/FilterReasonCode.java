package com.podads.domain.valueobjects;

public enum FilterReasonCode {
    CAMPAIGN_INACTIVE("campaign_inactive"),
    CAMPAIGN_ENDED("campaign_ended"),
    OUTSIDE_SCHEDULE_WINDOW("outside_schedule_window"),
    GEO_MISMATCH("geo_mismatch"),
    DEVICE_MISMATCH("device_mismatch"),
    TIER_MISMATCH("tier_mismatch"),
    CATEGORY_MISMATCH("category_mismatch"),
    SHOW_MISMATCH("show_mismatch"),
    EXCLUDED_CATEGORY("excluded_category"),
    DURATION_TOO_LONG("duration_too_long"),
    DURATION_TOO_SHORT("duration_too_short"),
    FREQUENCY_CAP_EXCEEDED("frequency_cap_exceeded"),
    BUDGET_EXHAUSTED("budget_exhausted"),
    PACING_LIMIT_EXCEEDED("pacing_limit_exceeded"),
    BRAND_SAFETY_VIOLATION("brand_safety_violation"),
    CREATIVE_NOT_APPROVED("creative_not_approved"),
    SLOT_TYPE_MISMATCH("slot_type_mismatch");

    private final String value;

    FilterReasonCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}


