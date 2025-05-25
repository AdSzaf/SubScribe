package com.example.subscribe.services;

import com.example.subscribe.models.Subscription;
import com.example.subscribe.database.SubscriptionDAO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SubscriptionService {
    private final SubscriptionDAO subscriptionDAO;

    public SubscriptionService() {
        this.subscriptionDAO = new SubscriptionDAO();
    }

    // Get all subscriptions
    public List<Subscription> getAllSubscriptions() {
        return subscriptionDAO.getAll();
    }

    // Get subscriptions asynchronously
    public CompletableFuture<List<Subscription>> getAllSubscriptionsAsync() {
        return CompletableFuture.supplyAsync(subscriptionDAO::getAll);
    }

    // Add subscription
    public void addSubscription(Subscription subscription) {
        if (subscription != null) {
            subscriptionDAO.insert(subscription);
        }
    }

    // Update subscription
    public void updateSubscription(Subscription subscription) {
        if (subscription != null && subscription.getId() != null) {
            subscriptionDAO.update(subscription);
        }
    }

    // Delete subscription
    public boolean deleteSubscription(Long id) {
        if (id != null) {
            subscriptionDAO.delete(id);
            return true;
        }
        return false;
    }

    // Find subscription by ID
    public Subscription findById(Long id) {
        return getAllSubscriptions().stream()
                .filter(sub -> sub.getId() != null && sub.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Find subscriptions by name
    public List<Subscription> findByName(String name) {
        return getAllSubscriptions().stream()
                .filter(sub -> sub.getName() != null &&
                        sub.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    // Get active subscriptions only
    public List<Subscription> getActiveSubscriptions() {
        return getAllSubscriptions().stream()
                .filter(Subscription::isActive)
                .toList();
    }

    // Get subscription count
    public int getSubscriptionCount() {
        return getAllSubscriptions().size();
    }

    // Get active subscription count
    public int getActiveSubscriptionCount() {
        return (int) getAllSubscriptions().stream()
                .filter(Subscription::isActive)
                .count();
    }
}