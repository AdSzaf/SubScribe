package com.example.subscribe.utils;

import org.apache.commons.lang3.StringUtils;

public class ValidationUtils {
    public static boolean isValidSubscriptionName(String name) {
        return StringUtils.isNotBlank(name) && name.length() <= 50;
    }
}
