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

    public void handleRegister() {
        System.out.println("\n--- REGISTER ACCOUNT ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.println("Select Role: 1. Store Manager | 2. Rental Desk Clerk | 3. Inventory Auditor");

        String roleInput = scanner.nextLine();
        int roleChoice = 2;
        try {
            roleChoice = Integer.parseInt(roleInput);
        } catch (NumberFormatException ignored) {}

        String role = switch (roleChoice) {
            case 1 -> "Store Manager";
            case 2 -> "Rental Desk Clerk";
            case 3 -> "Inventory Auditor";
            default -> "Rental Desk Clerk";
        };

        boolean success = userDaoObj.registerUser(username, password, role);
        if (success) {
            System.out.println("Account registered successfully! Status is set to PENDING.");
        } else {
            System.out.println("Registration failed.");
        }
    }

    public user handleLogin() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        user u = userDaoObj.loginUser(username, password);
        if (u != null) {
            System.out.println("Login successful! Welcome, " + u.username() + " (" + u.role() + ")");
            System.out.println("Account Status: " + u.status());
            return u;
        } else {
            System.out.println("Invalid username or password.");
            return null;
        }
    }
}