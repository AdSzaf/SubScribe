package com.example.subscribe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import com.example.subscribe.components.InteractiveCalendarComponent;
import com.example.subscribe.models.Subscription;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;

public class CalendarViewController implements Initializable {
    @FXML private InteractiveCalendarComponent calendarComponent;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    @FXML private Label monthLabel;

    private YearMonth currentMonth = YearMonth.now();

    private ObservableList<Subscription> subscriptions;

    public void setSubscriptions(ObservableList<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
        updateCalendar();
    }
    private void changeMonth(int delta) {
    currentMonth = currentMonth.plusMonths(delta);
    updateCalendar();
}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prevMonthBtn.setOnAction(e -> changeMonth(-1));
        nextMonthBtn.setOnAction(e -> changeMonth(1));
        updateCalendar();
    }

    public void updateCalendar() {
    if (subscriptions == null) return;
    List<LocalDate> paymentDates = subscriptions.stream()
        .map(Subscription::getNextPaymentDate)
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toList());
    calendarComponent.setMonth(currentMonth);
    calendarComponent.highlightPaymentDates(paymentDates);
    monthLabel.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());
    }
}