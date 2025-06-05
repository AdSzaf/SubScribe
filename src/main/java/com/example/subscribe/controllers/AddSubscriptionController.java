package com.example.subscribe.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.example.subscribe.models.Subscription;
import com.example.subscribe.utils.ConfigManager;
import com.example.subscribe.utils.ReflectionUtils;
import com.example.subscribe.models.Category;
import com.example.subscribe.events.EventBusManager;
import com.example.subscribe.events.SubscriptionAddedEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AddSubscriptionController {
    @FXML private TextField nameField;
    @FXML private TextField costField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextField currencyField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker nextPaymentDatePicker;
    @FXML private TextField billingCycleField;
    @FXML private CheckBox activeCheckBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField websiteField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    @FXML
    private void initialize() {
        categoryCombo.getItems().setAll(ReflectionUtils.loadAllCategories());
        currencyField.setText(ConfigManager.get("app.currency", "USD"));
        startDatePicker.setValue(LocalDate.now());
        nextPaymentDatePicker.setValue(LocalDate.now().plusMonths(1));
        activeCheckBox.setSelected(true);
    }
    

    @FXML
    private void saveSubscription() {
        String name = nameField.getText();
        String costText = costField.getText();

        // Validate name
        if (!com.example.subscribe.utils.ValidationUtils.isValidSubscriptionName(name)) {
            showAlert("Validation Error", "Subscription name cannot be empty and must be at most 50 characters.");
            return;
        }
        // Validate cost
        try {
            new java.math.BigDecimal(costText);
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Cost must be a valid number.");
            return;
        }
        try {
            Subscription sub = new Subscription();
            sub.setName(name);
            sub.setCost(new java.math.BigDecimal(costText));
            sub.setCurrency(currencyField.getText());
            sub.setCategory(categoryCombo.getValue());
            sub.setStartDate(startDatePicker.getValue());
            sub.setNextPaymentDate(nextPaymentDatePicker.getValue());
            sub.setBillingCycle(Integer.parseInt(billingCycleField.getText()));
            sub.setActive(activeCheckBox.isSelected());
            sub.setDescription(descriptionArea.getText());
            sub.setWebsite(websiteField.getText());

            EventBusManager.getInstance().post(new SubscriptionAddedEvent(sub));
            closeDialog();
        } catch (Exception e) {
            showAlert("Validation Error", "Please fill all fields correctly.");
        }
    }

    @FXML
    private void cancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}