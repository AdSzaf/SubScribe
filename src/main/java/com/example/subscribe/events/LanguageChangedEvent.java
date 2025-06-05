package com.example.subscribe.events;

public class LanguageChangedEvent {
    private final String language;

    public LanguageChangedEvent(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }
}
