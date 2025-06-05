package com.example.subscribe.utils;

import com.example.subscribe.models.Subscription;
import org.apache.commons.csv.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVExporter {
    public static void exportToCSV(List<Subscription> subscriptions, String filePath) throws IOException {
        try (Writer writer = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Name", "Cost", "Currency", "Category", "NextPaymentDate"))) {
            for (Subscription sub : subscriptions) {
                csvPrinter.printRecord(
                    sub.getName(),
                    sub.getCost(),
                    sub.getCurrency(),
                    sub.getCategory() != null ? sub.getCategory().getDisplayName() : "",
                    sub.getNextPaymentDate()
                );
            }
        }
    }

        public static List<Subscription> importFromCSV(String filePath) throws IOException {
        List<Subscription> imported = new ArrayList<>();
        try (Reader reader = new FileReader(filePath);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                Subscription sub = new Subscription(
                    null,
                    record.get("Name"),
                    new java.math.BigDecimal(record.get("Cost")),
                    record.get("Currency"),
                    null,
                    java.time.LocalDate.parse(record.get("NextPaymentDate")),
                    30,
                    null,
                    true
                );
                imported.add(sub);
            }
        }
        return imported;
    }
}