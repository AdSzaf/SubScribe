package com.example.subscribe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import com.example.subscribe.components.PieChartComponent;
import com.example.subscribe.models.Subscription;
import com.example.subscribe.models.Category;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import javafx.collections.ObservableList;
import java.util.ResourceBundle;

public class StatisticsController implements Initializable {
    @FXML private PieChartComponent pieChartComponent;

    private ObservableList<Subscription> subscriptions;

    public void setSubscriptions(ObservableList<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
        updateChart();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void updateChart() {
        if (subscriptions == null) return;
        Map<String, Double> categoryExpenses = subscriptions.stream()
            .filter(Subscription::isActive)
            .collect(Collectors.groupingBy(
                sub -> sub.getCategory() != null ? sub.getCategory().getDisplayName() : "Other",
                Collectors.summingDouble(sub -> sub.getCost() != null ? sub.getCost().doubleValue() : 0.0)
            ));
        pieChartComponent.updateData(categoryExpenses);
    }
}