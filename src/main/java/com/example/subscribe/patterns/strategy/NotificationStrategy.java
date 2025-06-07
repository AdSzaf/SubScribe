package com.example.subscribe.patterns.strategy;

import com.example.subscribe.models.Subscription;

public interface NotificationStrategy {
    void notify(Subscription subscription, String message);
}
