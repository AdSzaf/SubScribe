package com.example.subscribe.components;

import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class InteractiveCalendarComponent extends GridPane {
    private YearMonth currentMonth = YearMonth.now();
    private Set<LocalDate> paymentDates = new HashSet<>();
    private Set<LocalDate> holidayDates = new HashSet<>();
    private java.util.function.Consumer<LocalDate> onDateSelected;

    public void setOnDateSelected(java.util.function.Consumer<LocalDate> handler) {
    this.onDateSelected = handler;
}

    public InteractiveCalendarComponent() {
        setGridLinesVisible(true);
        buildCalendar();
    }

    private void buildCalendar() {
        getChildren().clear();
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Monday
        int daysInMonth = currentMonth.lengthOfMonth();

        // Add day labels
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            add(new Label(days[i]), i, 0);
        }

        int row = 1;
        int col = dayOfWeek - 1;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            Label label = new Label(String.valueOf(day));
            label.setPrefSize(40, 30); // Set cell size
            label.setStyle("-fx-alignment: center; -fx-border-color: #ccc; -fx-font-size: 14;");

            boolean isPayment = paymentDates.contains(date);
            boolean isHoliday = holidayDates.contains(date);

            if (isPayment && isHoliday) {
                label.setStyle(label.getStyle() + "-fx-background-color: orange; -fx-font-weight: bold;"); // Both
            } else if (isPayment) {
                label.setStyle(label.getStyle() + "-fx-background-color: yellow; -fx-font-weight: bold;");
            } else if (isHoliday) {
                label.setStyle(label.getStyle() + "-fx-background-color: #90caf9; -fx-font-weight: bold;");
            }
            add(label, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
            label.setOnMouseClicked(e -> {
                if (onDateSelected != null) onDateSelected.accept(date);
            });
        }
    }

    public void highlightPaymentDates(List<LocalDate> paymentDates) {
        this.paymentDates = new HashSet<>(paymentDates);
        buildCalendar();
    }
    public void highlightHolidayDates(List<LocalDate> holidayDates) {
        this.holidayDates = new HashSet<>(holidayDates);
        buildCalendar();
    }
    public void setMonth(YearMonth month) {
        this.currentMonth = month;
        buildCalendar();
    }
}