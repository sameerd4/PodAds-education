package com.podads.domain.valueobjects;

public enum DeviceType {
    MOBILE("mobile"),
    DESKTOP("desktop"),
    SMART_SPEAKER("smart-speaker"),
    CAR("car");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DeviceType fromString(String value) {
        for (DeviceType type : DeviceType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown device type: " + value);
    }
}


