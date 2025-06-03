package com.example.subscribe.events;

import com.example.subscribe.models.Subscription;

public class SubscriptionUpdatedEvent {
    private final Subscription subscription;

    public SubscriptionUpdatedEvent(Subscription subscription) {
        this.subscription = subscription;
    }

    public Subscription getSubscription() {
        return subscription;
    }
}