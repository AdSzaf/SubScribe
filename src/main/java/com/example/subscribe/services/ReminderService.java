package com.example.subscribe.services;

import com.example.subscribe.models.Subscription;
import com.example.subscribe.services.SubscriptionService;
import com.example.subscribe.events.EventBusManager;
import com.example.subscribe.events.PaymentDueEvent;
import com.example.subscribe.utils.ConfigManager;
import com.example.subscribe.patterns.strategy.NotificationStrategy;
import com.example.subscribe.patterns.strategy.AlertNotificationStrategy;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;

public class ReminderService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final SubscriptionService subscriptionService = new SubscriptionService();
    private NotificationStrategy notificationStrategy = new AlertNotificationStrategy();
    public void start() {
        int reminderDays = Integer.parseInt(ConfigManager.get("reminder.days.before", "3"));
        scheduler.scheduleAtFixedRate(() -> checkReminders(reminderDays), 5, 60, TimeUnit.SECONDS);
    }
    

    public void setNotificationStrategy(NotificationStrategy strategy) {
        this.notificationStrategy = strategy;
    }
    private void checkReminders(int reminderDays) {
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
        LocalDate now = LocalDate.now();
        LocalDate reminderDate = now.plusDays(reminderDays);

        for (Subscription sub : subscriptions) {
            if (sub.isActive() && sub.getNextPaymentDate() != null) {
                if (!sub.getNextPaymentDate().isBefore(now) && !sub.getNextPaymentDate().isAfter(reminderDate)) {
                    EventBusManager.getInstance().post(new PaymentDueEvent(sub));
                }
            }
        }
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}