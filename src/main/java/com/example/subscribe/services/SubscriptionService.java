package com.example.subscribe.services;

import com.example.subscribe.models.Subscription;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class SubscriptionService {
    private List<Subscription> subscriptions;

    public SubscriptionService() {
        this.subscriptions = new ArrayList<>();
    }

    // Get all subscriptions
    public List<Subscription> getAllSubscriptions() {
        return new ArrayList<>(subscriptions);
    }

    // Get subscriptions asynchronously
    public CompletableFuture<List<Subscription>> getAllSubscriptionsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate some processing time
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getAllSubscriptions();
        });
    }

    // Add subscription
    public void addSubscription(Subscription subscription) {
        if (subscription != null) {
            subscriptions.add(subscription);
        }
    }

    // Update subscription
    public void updateSubscription(Subscription subscription) {
        if (subscription != null && subscription.getId() != null) {
            for (int i = 0; i < subscriptions.size(); i++) {
                if (subscriptions.get(i).getId().equals(subscription.getId())) {
                    subscriptions.set(i, subscription);
                    break;
                }
            }
        }
    }

    // Delete subscription
    public boolean deleteSubscription(Long id) {
        return subscriptions.removeIf(sub -> sub.getId() != null && sub.getId().equals(id));
    }

    // Find subscription by ID
    public Subscription findById(Long id) {
        return subscriptions.stream()
                .filter(sub -> sub.getId() != null && sub.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Find subscriptions by name
    public List<Subscription> findByName(String name) {
        return subscriptions.stream()
                .filter(sub -> sub.getName() != null &&
                        sub.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    // Get active subscriptions only
    public List<Subscription> getActiveSubscriptions() {
        return subscriptions.stream()
                .filter(Subscription::isActive)
                .toList();
    }

    // Get subscription count
    public int getSubscriptionCount() {
        return subscriptions.size();
    }

    // Get active subscription count
    public int getActiveSubscriptionCount() {
        return (int) subscriptions.stream()
                .filter(Subscription::isActive)
                .count();
    }
}