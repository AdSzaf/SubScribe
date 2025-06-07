package com.example.subscribe.patterns.factory;

import com.example.subscribe.patterns.strategy.AlertNotificationStrategy;
import com.example.subscribe.patterns.strategy.ConsoleNotificationStrategy;
import com.example.subscribe.patterns.strategy.NotificationStrategy;

public class NotificationStrategyFactory {
    public static NotificationStrategy create(String type) {
        return switch (type.toLowerCase()) {
            case "console" -> new ConsoleNotificationStrategy();
            case "alert" -> new AlertNotificationStrategy();
            // add more strategies here
            default -> new AlertNotificationStrategy();
        };
    }
}