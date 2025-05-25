package com.example.subscribe.services;

import java.util.concurrent.CompletableFuture;
import java.math.BigDecimal;

public class CurrencyService {
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/USD";

    public CompletableFuture<BigDecimal> getExchangeRateAsync(String fromCurrency, String toCurrency) {
        return CompletableFuture.supplyAsync(() -> {
            // HTTP call to currency API
            return BigDecimal.ONE; // Placeholder
        });
    }
}