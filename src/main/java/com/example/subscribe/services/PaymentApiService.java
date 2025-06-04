package com.example.subscribe.services;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;

public class PaymentApiService {
    public static class PaymentTransaction {
        public String id;
        public String description;
        public double amount;
        public String currency;
        public String date;

        public PaymentTransaction(String id, String description, double amount, String currency, String date) {
            this.id = id;
            this.description = description;
            this.amount = amount;
            this.currency = currency;
            this.date = date;
        }
    }

    public CompletableFuture<List<PaymentTransaction>> fetchTransactionsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000); // Simulate network delay
            } catch (InterruptedException ignored) {}
            List<PaymentTransaction> transactions = new ArrayList<>();
            transactions.add(new PaymentTransaction("TXN001", "Netflix", 49.99, "PLN", "2025-06-01"));
            transactions.add(new PaymentTransaction("TXN002", "Spotify", 19.99, "PLN", "2025-06-02"));
            return transactions;
        });
    }
}