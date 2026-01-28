package com.podads.domain.valueobjects;

public enum PodcastCategory {
    FITNESS("fitness"),
    TECH("tech"),
    FINANCE("finance"),
    TRUE_CRIME("true-crime"),
    SPORTS("sports"),
    COMEDY("comedy"),
    NEWS("news"),
    EDUCATION("education");

    private final String value;

    PodcastCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PodcastCategory fromString(String value) {
        for (PodcastCategory category : PodcastCategory.values()) {
            if (category.value.equals(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown podcast category: " + value);
    }
}


