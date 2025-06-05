package com.example.subscribe.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.example.subscribe.utils.ConfigManager;
import com.example.subscribe.events.CurrencyChangedEvent;
import com.example.subscribe.events.EventBusManager;
import com.example.subscribe.events.LanguageChangedEvent;

public class SettingsController {
    @FXML private TextField currencyField;
    @FXML private TextField languageField;
    @FXML private TextField reminderDaysField;

    @FXML
    public void initialize() {
        currencyField.setText(ConfigManager.get("app.currency", "USD"));
        languageField.setText(ConfigManager.get("app.language", "en"));
        reminderDaysField.setText(ConfigManager.get("reminder.days.before", "3"));
    }

    @FXML
    private void saveSettings() {
        ConfigManager.set("app.currency", currencyField.getText());
        ConfigManager.set("app.language", languageField.getText());
        ConfigManager.set("reminder.days.before", reminderDaysField.getText());
        ConfigManager.saveConfig();
        EventBusManager.getInstance().post(new CurrencyChangedEvent());
        EventBusManager.getInstance().post(new LanguageChangedEvent(languageField.getText()));
        showAlert("Settings Saved", "Settings have been saved successfully.");
        close();
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) currencyField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setTitle(title);
        alert.showAndWait();
    }
}