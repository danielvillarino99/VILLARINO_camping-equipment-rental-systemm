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

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            // Enable SQLite Foreign Key constraints on initialization
            stmt.execute("PRAGMA foreign_keys = ON;");
            // Create the users table if it doesn't already exist
            stmt.execute(createUsersTable);
            System.out.println("Database and tables initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Table initialization error: " + e.getMessage());
        }
    }
}