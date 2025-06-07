package com.example.subscribe.patterns.strategy;

import com.example.subscribe.models.Subscription;

public class ConsoleNotificationStrategy implements NotificationStrategy {
    @Override
    public void notify(Subscription subscription, String message) {
        System.out.println(message);
    }
}