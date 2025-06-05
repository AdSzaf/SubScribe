package com.example.subscribe.utils;

import com.example.subscribe.models.Category;
import java.util.*;
import java.lang.reflect.Modifier;

public class ReflectionUtils {
    public static List<Category> loadAllCategories() {
        List<Category> categories = new ArrayList<>();
        try {
            // List all known category class names here or scan the package if using a library
            String[] categoryClassNames = {
                "com.example.subscribe.models.EntertainmentCategory",
                "com.example.subscribe.models.UtilitiesCategory"
                // Add more as needed
            };
            for (String className : categoryClassNames) {
                Class<?> clazz = Class.forName(className);
                if (Category.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                    categories.add((Category) clazz.getDeclaredConstructor().newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }
}