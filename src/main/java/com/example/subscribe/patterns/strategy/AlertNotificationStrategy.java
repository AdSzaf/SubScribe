package com.example.subscribe.patterns.strategy;

import com.example.subscribe.models.Subscription;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertNotificationStrategy implements NotificationStrategy {
    @Override
    public void notify(Subscription subscription, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Reminder");
            alert.setHeaderText("Upcoming Payment");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}