package com.example.subscribe.events;

import com.example.subscribe.models.Subscription;

public class SubscriptionEndingEvent {
    private final Subscription subscription;
    public SubscriptionEndingEvent(Subscription subscription) {
        this.subscription = subscription;
    }
    public Subscription getSubscription() {
        return subscription;
    }
}
