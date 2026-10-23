package com.scc;

import com.scc.dao.DatabaseConnection;
import com.scc.dao.UserDao;
import com.scc.model.user;
import com.scc.ui.AdminMenu;
import com.scc.ui.Authentication;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        initializeDatabase();

        UserDao userDaoObj = new UserDao();
        Scanner scanner = new Scanner(System.in);
        Authentication auth = new Authentication(userDaoObj, scanner);

        while (true) {
            System.out.println("\n=== CAMPING EQUIPMENT RENTAL SYSTEM ===");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-3).");
                continue;
            }

            switch (choice) {
                case 1 -> auth.handleRegister();
                case 2 -> {
                    user u = auth.handleLogin();
                    if (u != null) {
                        userSession(u);
                    }
                }
                case 3 -> {
                    System.out.println("Exiting application. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please choose 1, 2, or 3.");
            }
        }
    }

    private static void initializeDatabase() {
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
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(createUsersTable);
        } catch (Exception e) {
            System.out.println("Table initialization error: " + e.getMessage());
        }
    }

    private static void userSession(user u) {
        if ("Store Manager".equals(u.role())) {
            AdminMenu.displayAdminMenu();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- USER DASHBOARD (" + u.role() + ") ---");
            System.out.println("1. View Profile Info");
            System.out.println("2. Logout / Exit Session");
            System.out.print("Choose option: ");

            String input = scanner.nextLine();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter 1 or 2.");
                continue;
            }

            if (choice == 1) {
                System.out.println("User ID: " + u.user_id());
                System.out.println("Username: " + u.username());
                System.out.println("Role: " + u.role());
                System.out.println("Status: " + u.status());
            } else if (choice == 2) {
                System.out.println("Logging out safely...");
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }
}