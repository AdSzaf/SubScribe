package com.example.subscribe.models;

import java.time.LocalDate;
import java.math.BigDecimal;

public class Subscription {
    private Long id;
    private String name;
    private BigDecimal cost;
    private String currency;
    private LocalDate startDate;
    private LocalDate nextPaymentDate;
    private int billingCycle; // days
    private Category category;
    private boolean active;
    private String description;
    private String website;

    // Default constructor
    public Subscription() {
        this.active = true;
        this.currency = "USD";
        this.billingCycle = 30; // Default to monthly
    }

    // Constructor with required fields
    public Subscription(String name, BigDecimal cost, Category category) {
        this();
        this.name = name;
        this.cost = cost;
        this.category = category;
    }

    // Full constructor
    public Subscription(Long id, String name, BigDecimal cost, String currency,
                        LocalDate startDate, LocalDate nextPaymentDate,
                        int billingCycle, Category category, boolean active) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.currency = currency;
        this.startDate = startDate;
        this.nextPaymentDate = nextPaymentDate;
        this.billingCycle = billingCycle;
        this.category = category;
        this.active = active;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getNextPaymentDate() {
        return nextPaymentDate;
    }

    public int getBillingCycle() {
        return billingCycle;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setNextPaymentDate(LocalDate nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
    }

    public void setBillingCycle(int billingCycle) {
        this.billingCycle = billingCycle;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    // Utility methods
    public BigDecimal getMonthlyCost() {
        if (cost == null) return BigDecimal.ZERO;

        // Convert to monthly cost based on billing cycle
        if (billingCycle == 30) {
            return cost; // Already monthly
        } else if (billingCycle == 365) {
            return cost.divide(new BigDecimal("12"), 2, BigDecimal.ROUND_HALF_UP); // Annual to monthly
        } else if (billingCycle == 7) {
            return cost.multiply(new BigDecimal("4.33")); // Weekly to monthly (4.33 weeks per month)
        } else {
            // Custom calculation for other cycles
            return cost.multiply(new BigDecimal("30")).divide(new BigDecimal(billingCycle), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    public BigDecimal getAnnualCost() {
        if (cost == null) return BigDecimal.ZERO;

        // Convert to annual cost
        return getMonthlyCost().multiply(new BigDecimal("12"));
    }

    public void calculateNextPayment() {
        if (nextPaymentDate == null && startDate != null) {
            nextPaymentDate = startDate.plusDays(billingCycle);
        } else if (nextPaymentDate != null) {
            nextPaymentDate = nextPaymentDate.plusDays(billingCycle);
        }
    }

    public boolean isDueWithinDays(int days) {
        if (nextPaymentDate == null || !active) return false;
        return nextPaymentDate.isBefore(LocalDate.now().plusDays(days + 1));
    }

    public boolean isOverdue() {
        if (nextPaymentDate == null || !active) return false;
        return nextPaymentDate.isBefore(LocalDate.now());
    }

    // Override methods for proper object handling
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Subscription that = (Subscription) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cost=" + cost +
                ", currency='" + currency + '\'' +
                ", category=" + category +
                ", active=" + active +
                ", nextPaymentDate=" + nextPaymentDate +
                '}';
    }
}