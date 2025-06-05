package com.example.subscribe.utils;

import com.example.subscribe.models.Category;
import java.util.*;
import java.lang.reflect.Modifier;

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
                // "com.example.subscribe.models.GamingCategory",
                // "com.example.subscribe.models.CommunicationCategory",
                // "com.example.subscribe.models.BusinessCategory",
                // "com.example.subscribe.models.UtilitiesCategory",
                // "com.example.subscribe.models.FinanceCategory",
                // "com.example.subscribe.models.ShoppingCategory",
                // "com.example.subscribe.models.FoodDeliveryCategory",
                // "com.example.subscribe.models.TransportationCategory",
                // "com.example.subscribe.models.SecurityCategory",
                // "com.example.subscribe.models.DesignCategory",
                // "com.example.subscribe.models.DevelopmentCategory",
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