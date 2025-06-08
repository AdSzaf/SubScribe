package com.example.subscribe.patterns.factory;

import com.example.subscribe.models.Subscription;
import com.example.subscribe.patterns.strategy.AlertNotificationStrategy;
import com.example.subscribe.patterns.strategy.ConsoleNotificationStrategy;
import com.example.subscribe.patterns.strategy.EndOfSubscriptionNotificationStrategy;
import com.example.subscribe.patterns.strategy.NotificationStrategy;

public class NotificationStrategyFactory {
    public static NotificationStrategy create(String type) {
        return create(type, null, null);
    }

    public static NotificationStrategy create(String type,
        java.util.function.Consumer<Subscription> onProlong,
        java.util.function.Consumer<Subscription> onCancel) {
        return switch (type.toLowerCase()) {
            case "end" -> new EndOfSubscriptionNotificationStrategy(onProlong, onCancel);
            case "console" -> new ConsoleNotificationStrategy();
            case "alert" -> new AlertNotificationStrategy();
            default -> new AlertNotificationStrategy();
        };
    }
}