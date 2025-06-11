package com.example.subscribe.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class TranslationService {
    private static final String API_URL = "https://libretranslate.com/translate";
    private static final HttpClient client = HttpClient.newHttpClient();

    public CompletableFuture<String> translate(String text, String targetLang) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("q", text);
                json.put("source", "auto");
                json.put("target", targetLang);
                json.put("format", "text");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject result = new JSONObject(response.body());

                if (result.has("error")) {
                    System.err.println("Translation API error: " + result.getString("error"));
                    return text; // fallback to original if error
                }
                return result.getString("translatedText");
            } catch (Exception e) {
                e.printStackTrace();
                return text; // fallback to original if error
            }
        });
    }

    public CompletableFuture<List<String>> translate(List<String> texts, String targetLang) {

        List<CompletableFuture<String>> futures = texts.stream()
                .map(text -> translate(text, targetLang))
                .collect(Collectors.toList());
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }
}