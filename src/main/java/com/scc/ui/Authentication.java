package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.user;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDaoObj;
    private final Scanner scanner;

    public Authentication(UserDao userDaoObj, Scanner scanner) {
        this.userDaoObj = userDaoObj;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("     CAMPING EQUIPMENT RENTAL SYSTEM              ");
            System.out.println("==================================================");
            System.out.println("[1] Register Account");
            System.out.println("[2] Login");
            System.out.println("[3] Exit");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-3).");
                continue;
            }

            switch (choice) {
                case 1 -> handleRegister();
                case 2 -> {
                    user u = handleLogin();
                    if (u != null) {
                        runUserSession(u);
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

    public void handleRegister() {
        System.out.println("\n--- USER REGISTRATION ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        System.out.println("Select Role:");
        System.out.println("[1] System Admin");
        System.out.println("[2] Rental Desk Clerk");
        System.out.println("[3] Inventory Auditor");
        System.out.print("Choose role option: ");

        String roleInput = scanner.nextLine().trim();
        int roleChoice = 2;
        try {
            roleChoice = Integer.parseInt(roleInput);
        } catch (NumberFormatException ignored) {}

        String role = switch (roleChoice) {
            case 1 -> "System Admin";
            case 2 -> "Rental Desk Clerk";
            case 3 -> "Inventory Auditor";
            default -> {
                System.out.println("Invalid role choice. Defaulting to Rental Desk Clerk.");
                yield "Rental Desk Clerk";
            }
        };

        boolean success = userDaoObj.registerUser(username, password, role);
        if (success) {
            if ("System Admin".equalsIgnoreCase(role)) {
                System.out.println("Registration successful! System Admin account is automatically APPROVED.");
            } else {
                System.out.println("Registration successful! Account status is PENDING approval.");
            }
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    public user handleLogin() {
        System.out.println("\n--- USER LOGIN ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        user u = userDaoObj.loginUser(username, password);

        if (u == null) {
            System.out.println("Login failed: Incorrect username or password.");
            return null;
        }

        if (!"APPROVED".equalsIgnoreCase(u.status())) {
            System.out.println("Login restricted: Your account is still PENDING approval.");
            return null;
        }

        System.out.println("Login successful! Welcome, " + u.username() + " (" + u.role() + ")");
        return u;
    }

    private void runUserSession(user u) {
        // Route System Admin directly into the Admin Menu automatically upon login
        if ("System Admin".equals(u.role())) {
            AdminMenu.displayAdminMenu(u, userDaoObj, scanner);
            return;
        }

        // Default session dashboard for non-admin roles
        while (true) {
            System.out.println("\n--- USER DASHBOARD (" + u.role() + ") ---");
            System.out.println("[1] View Profile Info");
            System.out.println("[2] Logout / Exit Session");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine().trim();
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