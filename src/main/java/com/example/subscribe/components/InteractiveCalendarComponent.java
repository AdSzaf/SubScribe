package com.example.subscribe.components;


import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import java.time.LocalDate;

public class InteractiveCalendarComponent extends GridPane {

    public InteractiveCalendarComponent() {
        initializeComponent();
    }

    private void initializeComponent() {
        // Calendar grid setup
        setGridLinesVisible(true);
        buildCalendar();
    }

    private void buildCalendar() {
        // Calendar building logic
    }

    public void highlightPaymentDates(java.util.List<LocalDate> paymentDates) {
        // Highlight specific dates
    }
}
