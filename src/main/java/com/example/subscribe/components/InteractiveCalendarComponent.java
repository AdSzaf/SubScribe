package com.example.subscribe.components;

import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class InteractiveCalendarComponent extends GridPane {
    private YearMonth currentMonth = YearMonth.now();
    private List<LocalDate> paymentDates;

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
            if (paymentDates != null && paymentDates.contains(date)) {
                label.setStyle(label.getStyle() + "-fx-background-color: yellow; -fx-font-weight: bold;");
            }
            add(label, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    public void highlightPaymentDates(List<LocalDate> paymentDates) {
        this.paymentDates = paymentDates;
        buildCalendar();
    }
    public void setMonth(YearMonth month) {
    this.currentMonth = month;
    buildCalendar();
}
}