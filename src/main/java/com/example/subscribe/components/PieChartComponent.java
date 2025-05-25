package com.example.subscribe.components;

import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;
import java.util.Map;

public class PieChartComponent extends VBox {
    private PieChart pieChart;

    public PieChartComponent() {
        initializeComponent();
    }

    private void initializeComponent() {
        pieChart = new PieChart();
        pieChart.setTitle("Expenses by Category");
        getChildren().add(pieChart);
    }

    public void updateData(Map<String, Double> categoryExpenses) {
        // Update pie chart data
    }
}