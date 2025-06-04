package com.example.subscribe.events;

public class CalendarExportedEvent {
    private final boolean success;
    private final String message;

    public CalendarExportedEvent(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}