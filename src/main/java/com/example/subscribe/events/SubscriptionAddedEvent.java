package com.example.subscribe.events;

import com.example.subscribe.models.Subscription;

public class SubscriptionAddedEvent {
    private final Subscription subscription;
    private final String source;

    public SubscriptionAddedEvent(Subscription subscription) {
        this(subscription, "Unknown");
    }

    public SubscriptionAddedEvent(Subscription subscription, String source) {
        this.subscription = subscription;
        this.source = source;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "SubscriptionAddedEvent{" +
                "subscription=" + subscription.getName() +
                ", source='" + source + '\'' +
                '}';
    }
}