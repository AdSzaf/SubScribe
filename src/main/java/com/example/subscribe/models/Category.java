package com.example.subscribe.models;

public enum Category {
    ENTERTAINMENT("Entertainment"),
    PRODUCTIVITY("Productivity"),
    CLOUD_STORAGE("Cloud Storage"),
    SOFTWARE("Software"),
    MUSIC_STREAMING("Music & Streaming"),
    VIDEO_STREAMING("Video Streaming"),
    NEWS_MEDIA("News & Media"),
    FITNESS_HEALTH("Fitness & Health"),
    EDUCATION("Education"),
    GAMING("Gaming"),
    COMMUNICATION("Communication"),
    BUSINESS("Business"),
    UTILITIES("Utilities"),
    FINANCE("Finance"),
    SHOPPING("Shopping"),
    FOOD_DELIVERY("Food & Delivery"),
    TRANSPORTATION("Transportation"),
    SECURITY("Security"),
    DESIGN("Design"),
    DEVELOPMENT("Development"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    // Utility method to get category by display name
    public static Category fromDisplayName(String displayName) {
        for (Category category : Category.values()) {
            if (category.getDisplayName().equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        return OTHER; // Default fallback
    }

    // Get categories for specific use cases
    public static Category[] getStreamingCategories() {
        return new Category[]{
                ENTERTAINMENT,
                MUSIC_STREAMING,
                VIDEO_STREAMING
        };
    }

    public static Category[] getBusinessCategories() {
        return new Category[]{
                PRODUCTIVITY,
                BUSINESS,
                CLOUD_STORAGE,
                SOFTWARE,
                DEVELOPMENT
        };
    }

    public static Category[] getPersonalCategories() {
        return new Category[]{
                ENTERTAINMENT,
                FITNESS_HEALTH,
                EDUCATION,
                GAMING,
                FOOD_DELIVERY
        };
    }
}