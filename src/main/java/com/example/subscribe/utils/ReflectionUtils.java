package com.example.subscribe.utils;

import com.example.subscribe.models.Category;
import java.util.*;

public class ReflectionUtils {
    public static List<Category> loadAllCategories() {
        List<Category> categories = new ArrayList<>();
        try {
            String[] categoryClassNames = {
                "com.example.subscribe.models.Categories.EntertainmentCategory",
                "com.example.subscribe.models.Categories.ProductivityCategory",
                "com.example.subscribe.models.Categories.CloudStorageCategory",
                "com.example.subscribe.models.Categories.SoftwareCategory",
                "com.example.subscribe.models.Categories.MusicStreamingCategory",
                "com.example.subscribe.models.Categories.VideoStreamingCategory",
                "com.example.subscribe.models.Categories.NewsMediaCategory",
                "com.example.subscribe.models.Categories.FitnessHealthCategory",
                "com.example.subscribe.models.Categories.EducationCategory",
                "com.example.subscribe.models.Categories.OtherCategory"
            };
            for (String className : categoryClassNames) {
                Class<?> clazz = Class.forName(className);
                categories.add((Category) clazz.getDeclaredConstructor().newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }
}