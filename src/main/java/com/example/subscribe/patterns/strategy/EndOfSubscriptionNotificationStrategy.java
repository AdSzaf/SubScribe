package com.example.subscribe.patterns.strategy;

import com.example.subscribe.models.Subscription;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.time.LocalDate;

public class EndOfSubscriptionNotificationStrategy implements NotificationStrategy {
    private final java.util.function.Consumer<Subscription> onProlong;
    private final java.util.function.Consumer<Subscription> onCancel;

    public EndOfSubscriptionNotificationStrategy(
        java.util.function.Consumer<Subscription> onProlong,
        java.util.function.Consumer<Subscription> onCancel
    ) {
        this.onProlong = onProlong;
        this.onCancel = onCancel;
    }

    @Override
    public void notify(Subscription subscription, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Subscription Ending");
            alert.setHeaderText("Subscription \"" + subscription.getName() + "\" is ending today!");
            alert.setContentText(message);

            ButtonType prolongBtn = new ButtonType("Prolong");
            ButtonType cancelBtn = new ButtonType("Cancel Subscription");
            ButtonType closeBtn = ButtonType.CLOSE;

            alert.getButtonTypes().setAll(prolongBtn, cancelBtn, closeBtn);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == prolongBtn) {
                    onProlong.accept(subscription);
                } else if (result.get() == cancelBtn) {
                    onCancel.accept(subscription);
                }
            }
        });
    }
}