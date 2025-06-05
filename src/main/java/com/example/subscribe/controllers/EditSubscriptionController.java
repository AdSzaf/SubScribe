package com.example.subscribe.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.example.subscribe.models.Subscription;
import com.example.subscribe.utils.ReflectionUtils;
import com.example.subscribe.models.Category;
import com.example.subscribe.events.EventBusManager;
import com.example.subscribe.events.SubscriptionAddedEvent; // You should create SubscriptionUpdatedEvent for clarity

import java.math.BigDecimal;
import java.time.LocalDate;

public class EditSubscriptionController {
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

    private Subscription subscription;

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
        // Populate fields
        nameField.setText(subscription.getName());
        costField.setText(subscription.getCost() != null ? subscription.getCost().toString() : "");
        currencyField.setText(subscription.getCurrency());
        categoryCombo.getItems().setAll(ReflectionUtils.loadAllCategories());
        categoryCombo.setValue(subscription.getCategory());
        startDatePicker.setValue(subscription.getStartDate());
        nextPaymentDatePicker.setValue(subscription.getNextPaymentDate());
        billingCycleField.setText(String.valueOf(subscription.getBillingCycle()));
        activeCheckBox.setSelected(subscription.isActive());
        descriptionArea.setText(subscription.getDescription());
        websiteField.setText(subscription.getWebsite());
    }

    @FXML
    private void initialize() {
        categoryCombo.getItems().setAll(ReflectionUtils.loadAllCategories());
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
            subscription.setName(name);
            subscription.setCost(new java.math.BigDecimal(costText));
            subscription.setCurrency(currencyField.getText());
            subscription.setCategory(categoryCombo.getValue());
            subscription.setStartDate(startDatePicker.getValue());
            subscription.setNextPaymentDate(nextPaymentDatePicker.getValue());
            subscription.setBillingCycle(Integer.parseInt(billingCycleField.getText()));
            subscription.setActive(activeCheckBox.isSelected());
            subscription.setDescription(descriptionArea.getText());
            subscription.setWebsite(websiteField.getText());

            // Post update event (create SubscriptionUpdatedEvent class)
            EventBusManager.getInstance().post(new com.example.subscribe.events.SubscriptionUpdatedEvent(subscription));
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