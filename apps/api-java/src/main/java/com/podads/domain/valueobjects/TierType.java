package com.podads.domain.valueobjects;

public enum TierType {
    FREE("free"),
    PREMIUM("premium");

    private final String value;

    TierType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TierType fromString(String value) {
        for (TierType type : TierType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown tier type: " + value);
    }
}


