package com.example.subscribe.events;


import com.google.common.eventbus.EventBus;

public class EventBusManager {
    private static EventBusManager instance;
    private final EventBus eventBus;

    private EventBusManager() {
        this.eventBus = new EventBus("SubScribe EventBus");
    }

    public static synchronized EventBusManager getInstance() {
        if (instance == null) {
            instance = new EventBusManager();
        }
        return instance;
    }

    public void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    public void unregister(Object subscriber) {
        eventBus.unregister(subscriber);
    }

    public void post(Object event) {
        eventBus.post(event);
    }

    // For testing or debugging
    public EventBus getEventBus() {
        return eventBus;
    }
}