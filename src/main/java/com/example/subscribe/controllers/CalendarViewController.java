package com.example.subscribe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import com.example.subscribe.components.InteractiveCalendarComponent;
import com.example.subscribe.events.EventBusManager;
import com.example.subscribe.events.PublicHolidaysFetchedEvent;
import com.example.subscribe.models.Subscription;
import com.example.subscribe.services.PublicHolidaysService;
import com.google.common.eventbus.Subscribe;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.ObservableList;

public class CalendarViewController implements Initializable {
    @FXML private InteractiveCalendarComponent calendarComponent;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    @FXML private Label monthLabel;
    @FXML private VBox explanationBox;
    @FXML private Label dateDetailsLabel;

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

    private List<PublicHolidaysService.Holiday> holidays = new ArrayList<>();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prevMonthBtn.setOnAction(e -> changeMonth(-1));
        nextMonthBtn.setOnAction(e -> changeMonth(1));
        EventBusManager.getInstance().register(this);
        calendarComponent.setOnDateSelected(this::showDateDetails);
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
        calendarComponent.highlightHolidayDates(
            holidays.stream().map(h -> LocalDate.parse(h.date)).collect(Collectors.toList()) // dla każdego obiektu h który jest Holiday weź h.date czyli string i zrób z niego local date
        );
        monthLabel.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());
    }

    @Subscribe
    public void onPublicHolidaysFetched(PublicHolidaysFetchedEvent event) {
        Platform.runLater(() -> {
            holidays.clear();
            holidays.addAll(event.getHolidays());
            updateCalendar();
        });
    }
    @FXML
    public void onClose() {
        EventBusManager.getInstance().unregister(this);
    }

    private void showDateDetails(LocalDate date) {
        StringBuilder sb = new StringBuilder(date.toString() + ":\n");

        List<Subscription> dueSubs = subscriptions.stream()
            .filter(s -> date.equals(s.getNextPaymentDate()))
            .collect(Collectors.toList());
        if (!dueSubs.isEmpty()) {
            sb.append("Subscriptions due:\n");
            dueSubs.forEach(s -> sb.append("• ").append(s.getName()).append("\n"));
        }

        List<PublicHolidaysService.Holiday> holidaysOnDate = holidays.stream()
            .filter(h -> LocalDate.parse(h.date).equals(date))
            .collect(Collectors.toList());
        if (!holidaysOnDate.isEmpty()) {
            sb.append("Holiday(s):\n");
            holidaysOnDate.forEach(h -> sb.append("• ").append(h.name).append("\n"));
        }

        if (sb.length() == (date.toString() + ":\n").length()) {
            sb.append("No events.");
        }
        dateDetailsLabel.setText(sb.toString());
    }
}