package com.example.subscribe.events;

import com.example.subscribe.models.Subscription;

public class PaymentDueEvent {
    private final Subscription subscription;

    public PaymentDueEvent(Subscription subscription) {
        this.subscription = subscription;
    }

    public Subscription getSubscription() {
        return subscription;
    }
}