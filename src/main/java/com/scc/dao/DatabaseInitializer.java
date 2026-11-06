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

        String createInventoryTable = """
            CREATE TABLE IF NOT EXISTS equipment_inventory (
                item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                item_name TEXT NOT NULL,
                category TEXT NOT NULL,
                daily_rental_rate REAL NOT NULL,
                total_stock INTEGER NOT NULL,
                availability_status TEXT NOT NULL,
                condition_rating INTEGER NOT NULL
            );
        """;

        String createRentalsTable = """
            CREATE TABLE IF NOT EXISTS rental_orders (
                rental_id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id INTEGER NOT NULL,
                customer_name TEXT NOT NULL,
                item_id INTEGER NOT NULL,
                item_name TEXT NOT NULL,
                rental_duration_days INTEGER NOT NULL,
                total_amount REAL NOT NULL,
                order_status TEXT NOT NULL,
                penalty_fee REAL DEFAULT 0.0,
                FOREIGN KEY (item_id) REFERENCES equipment_inventory(item_id)
            );
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(createUsersTable);
            stmt.execute(createActivityLogsTable);
            stmt.execute(createInventoryTable);
            stmt.execute(createRentalsTable);
            System.out.println("Database and tables initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Table initialization error: " + e.getMessage());
        }
    }
}