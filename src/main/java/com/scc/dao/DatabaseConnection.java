package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:RentalRecords.db";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            // Set busy timeout to 3000ms to wait if database is busy
            stmt.execute("PRAGMA busy_timeout = 3000;");
            // Enable Foreign Keys
            stmt.execute("PRAGMA foreign_keys = ON;");
            // Enable Write-Ahead Logging to prevent lock errors
            stmt.execute("PRAGMA journal_mode = WAL;");
        }
        return conn;
    }
}