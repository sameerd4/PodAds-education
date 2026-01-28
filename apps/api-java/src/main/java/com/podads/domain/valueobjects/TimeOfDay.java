package com.podads.domain.valueobjects;

public enum TimeOfDay {
    MORNING("morning"),
    AFTERNOON("afternoon"),
    EVENING("evening"),
    NIGHT("night");

    private final String value;

    TimeOfDay(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TimeOfDay fromString(String value) {
        for (TimeOfDay time : TimeOfDay.values()) {
            if (time.value.equals(value)) {
                return time;
            }
        }
        throw new IllegalArgumentException("Unknown time of day: " + value);
    }
}


