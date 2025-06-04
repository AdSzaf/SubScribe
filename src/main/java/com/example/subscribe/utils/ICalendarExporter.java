package com.example.subscribe.utils;

import com.example.subscribe.models.Subscription;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ICalendarExporter {
    public static void exportSubscriptionsToICS(List<Subscription> subscriptions, String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//SubScribe//EN\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (Subscription sub : subscriptions) {
            if (sub.getNextPaymentDate() != null) {
                sb.append("BEGIN:VEVENT\n");
                sb.append("SUMMARY:").append(sub.getName()).append(" Payment Due\n");
                sb.append("DTSTART;VALUE=DATE:").append(sub.getNextPaymentDate().format(dtf)).append("\n");
                sb.append("DESCRIPTION:Payment for ").append(sub.getName()).append("\n");
                sb.append("END:VEVENT\n");
            }
        }
        sb.append("END:VCALENDAR\n");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(sb.toString());
        }
    }
}