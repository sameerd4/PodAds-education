package com.podads.domain.valueobjects;

public enum SlotType {
    PRE_ROLL("pre-roll"),
    MID_ROLL("mid-roll"),
    POST_ROLL("post-roll");

    private final String value;

    SlotType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SlotType fromString(String value) {
        for (SlotType type : SlotType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown slot type: " + value);
    }
}


