package com.example.subscribe.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:subscriptions.db";

    private DatabaseManager() {
        // Private constructor for singleton
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initDatabase() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        createTables();
    }

    private void createTables() throws SQLException {
        String createSubscriptionsTable = """
        CREATE TABLE IF NOT EXISTS subscriptions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            cost REAL NOT NULL,
            currency TEXT NOT NULL,
            start_date TEXT,
            next_payment_date TEXT,
            billing_cycle INTEGER,
            category TEXT,
            active INTEGER,
            description TEXT,
            website TEXT
        );
    """;

    String createRemindersTable = """
        CREATE TABLE IF NOT EXISTS reminders (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            subscription_id INTEGER,
            remind_days_before INTEGER,
            FOREIGN KEY(subscription_id) REFERENCES subscriptions(id)
        );
    """;

    try (var stmt = connection.createStatement()) {
        stmt.execute(createSubscriptionsTable);
        stmt.execute(createRemindersTable);
    }
    }

    public Connection getConnection() {
        return connection;
    }
}