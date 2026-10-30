package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    public static void displayAdminMenu(user currentUser, UserDao userDaoObj, Scanner scanner) {
        while (true) {
            System.out.println("\n=== ADMIN MANAGEMENT MENU ===");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("5. Exit Admin Menu");
            System.out.print("Choose option: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    List<user> allUsers = userDaoObj.getAllUsers();
                    printUserTable("ALL REGISTERED USERS", allUsers);
                }
                case 2 -> {
                    List<user> pendingUsers = userDaoObj.getPendingUsers();
                    printUserTable("PENDING REGISTRATIONS", pendingUsers);
                }
                case 3 -> {
                    List<user> pendingUsers = userDaoObj.getPendingUsers();
                    printUserTable("PENDING REGISTRATIONS", pendingUsers);

                    if (pendingUsers.isEmpty()) {
                        System.out.println("No accounts currently pending approval.");
                        break;
                    }

                    int targetId = readIntInput(scanner, "\nEnter Target User ID to approve (or enter 0 to cancel): ");
                    if (targetId == 0) {
                        System.out.println("Approval operation canceled.");
                        break;
                    }

                    boolean existsInPending = pendingUsers.stream().anyMatch(u -> u.user_id() == targetId);
                    if (!existsInPending) {
                        System.out.println("Error: User ID " + targetId + " is not in the pending list.");
                    } else {
                        boolean approved = userDaoObj.approveUser(targetId);
                        if (approved) {
                            System.out.println("SUCCESS: User ID " + targetId + " has been APPROVED!");
                        } else {
                            System.out.println("Failed to update user status.");
                        }
                    }
                }
                case 4 -> updateUserRoleAndStatus(currentUser, userDaoObj, scanner);
                case 5 -> {
                    System.out.println("Exiting Admin Menu...");
                    return;
                }
                default -> System.out.println("Invalid option. Choose between 1 and 5.");
            }
        }
    }

    private static void updateUserRoleAndStatus(user currentUser, UserDao userDaoObj, Scanner scanner) {
        List<user> allUsers = userDaoObj.getAllUsers();
        printUserTable("ALL REGISTERED USERS", allUsers);

        int targetId = readIntInput(scanner, "Enter User ID to UPDATE (or enter 0 to cancel): ");
        if (targetId == 0) {
            System.out.println("Update action cancelled.");
            return;
        }

        // Self-Edit Restriction Rule[cite: 1]
        if (targetId == currentUser.user_id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        boolean exists = allUsers.stream().anyMatch(u -> u.user_id() == targetId);
        if (!exists) {
            System.out.println("Invalid User ID. User does not exist.");
            return;
        }

        String newRole = readValidRole(scanner);
        String newStatus = readValidStatus(scanner);

        boolean success = userDaoObj.updateUserRoleAndStatus(targetId, newRole, newStatus);
        if (success) {
            System.out.println("SUCCESS: User ID " + targetId + " updated to Role: " + newRole + ", Status: " + newStatus);
        } else {
            System.out.println("Failed to update user account.");
        }
    }

    // --- Input Validation Wrappers[cite: 1] ---

    private static int readIntInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid numeric User ID.");
            }
        }
    }

    private static String readValidRole(Scanner scanner) {
        while (true) {
            System.out.print("Enter valid role (e.g., System Admin, Rental Desk Clerk, Inventory Auditor): ");
            String role = scanner.nextLine().trim();
            if (!role.isEmpty()) {
                return role.toUpperCase();
            }
            System.out.println("Role cannot be empty. Please try again.");
        }
    }

    private static String readValidStatus(Scanner scanner) {
        while (true) {
            System.out.print("Enter valid status (APPROVED, PENDING): ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("Invalid status. Only APPROVED or PENDING are accepted.");
        }
    }

    private static void printUserTable(String title, List<user> userList) {
        System.out.println("\n--- " + title + " ---");
        if (userList.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        String border = "+-------+-----------------+----------------------+------------+";
        System.out.println(border);
        System.out.printf("| %-5s | %-15s | %-20s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println(border);

        for (user u : userList) {
            System.out.printf("| %-5d | %-15s | %-20s | %-10s |\n",
                    u.user_id(), u.username(), u.role(), u.status());
        }
        System.out.println(border);
    }
}