package com.example.subscribe.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class PublicHolidaysService {
    private static final HttpClient client = HttpClient.newHttpClient();

    public CompletableFuture<List<Holiday>> fetchHolidays(int year, String countryCode) {
        String url = String.format("https://date.nager.at/api/v3/PublicHolidays/%d/%s", year, countryCode);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JSONArray arr = new JSONArray(response.body());
                List<Holiday> holidays = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    holidays.add(new Holiday(obj.getString("date"), obj.getString("localName")));
                }
                return holidays;
            } catch (Exception e) {
                e.printStackTrace();
                return List.of();
            }
        });
    }

    public static class Holiday {
        public final String date;
        public final String name;
        public Holiday(String date, String name) {
            this.date = date;
            this.name = name;
        }
    }
}