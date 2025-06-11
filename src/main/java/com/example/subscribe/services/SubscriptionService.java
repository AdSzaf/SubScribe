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

    public List<Subscription> getAllSubscriptions() {
        return subscriptionDAO.getAll();
    }

    public CompletableFuture<List<Subscription>> getAllSubscriptionsAsync() {
        return CompletableFuture.supplyAsync(subscriptionDAO::getAll);
    }

    public void addSubscription(Subscription subscription) {
        if (subscription != null) {
            subscriptionDAO.insert(subscription);
        }
    }

    public void updateSubscription(Subscription subscription) {
        if (subscription != null && subscription.getId() != null) {
            subscriptionDAO.update(subscription);
        }
    }

    public boolean deleteSubscription(Long id) {
        if (id != null) {
            subscriptionDAO.delete(id);
            return true;
        }
        return false;
    }

    public Subscription findById(Long id) {
        return getAllSubscriptions().stream()
                .filter(sub -> sub.getId() != null && sub.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Subscription> findByName(String name) {
        return getAllSubscriptions().stream()
                .filter(sub -> sub.getName() != null &&
                        sub.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    public List<Subscription> getActiveSubscriptions() {
        return getAllSubscriptions().stream()
                .filter(Subscription::isActive)
                .toList();
    }

    public int getSubscriptionCount() {
        return getAllSubscriptions().size();
    }

    public int getActiveSubscriptionCount() {
        return (int) getAllSubscriptions().stream()
                .filter(Subscription::isActive)
                .count();
    }
}