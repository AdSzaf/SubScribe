package com.example.subscribe.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.math.BigDecimal;
import org.json.JSONObject;

public class CurrencyService {
    // private static final String API_KEY = "4fdafa156eb049ed9bd0f8f27a47aab5";
    // private static final String API_URL = "https://api.currencyfreaks.com/v2.0/rates/latest?apikey=%s&base=%s";

    public CompletableFuture<BigDecimal> getExchangeRateAsync(String fromCurrency, String toCurrency) {
            return CompletableFuture.supplyAsync(() -> {
            try {
                String url = String.format(
                    "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/%s.json",
                    fromCurrency.toLowerCase()
                );
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject json = new JSONObject(response.body());

                if (!json.has(fromCurrency.toLowerCase())) {
                    throw new RuntimeException("API response missing base currency field.");
                }
                JSONObject rates = json.getJSONObject(fromCurrency.toLowerCase());
                if (!rates.has(toCurrency.toLowerCase())) {
                    throw new RuntimeException("Currency not found in rates: " + toCurrency);
                }
                double rate = rates.getDouble(toCurrency.toLowerCase());
                return BigDecimal.valueOf(rate);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error fetching exchange rate", e);
            }
        });
    }
}
