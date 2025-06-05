package com.example.subscribe.models;

import com.example.subscribe.utils.ReflectionUtils;
import com.example.subscribe.models.Categories.OtherCategory;

public abstract class Category {
    public abstract String getDisplayName();

    @Override
    public String toString() {
        return getDisplayName();
    }

    // Utility: Find category by display name
    public static Category fromDisplayName(String displayName) {
        for (Category cat : ReflectionUtils.loadAllCategories()) {
            if (cat.getDisplayName().equalsIgnoreCase(displayName)) {
                return cat;
            }
        }
        return new OtherCategory();
    }
}