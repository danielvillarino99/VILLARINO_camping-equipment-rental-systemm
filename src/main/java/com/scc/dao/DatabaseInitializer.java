package com.scc.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {
    public static void initializeDatabase() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL,
                status TEXT NOT NULL
            );
        """;

        String createActivityLogsTable = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                action_type TEXT NOT NULL,
                description TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            );
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(createUsersTable);
            stmt.execute(createActivityLogsTable);
            System.out.println("Database and tables initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Table initialization error: " + e.getMessage());
        }
    }
}