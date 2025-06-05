package com.example.subscribe.events;

import java.util.List;
import com.example.subscribe.services.PublicHolidaysService;

public class PublicHolidaysFetchedEvent {
    private final List<PublicHolidaysService.Holiday> holidays;
    public PublicHolidaysFetchedEvent(List<PublicHolidaysService.Holiday> holidays) {
        this.holidays = holidays;
    }
    public List<PublicHolidaysService.Holiday> getHolidays() {
        return holidays;
    }
}