package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    public static void displayAdminMenu() {
        UserDao userDaoObj = new UserDao();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== ADMIN MANAGEMENT MENU ===");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Exit Admin Menu");
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

                    System.out.print("\nEnter Target User ID to approve (or enter 0 to cancel): ");
                    String idInput = scanner.nextLine().trim();
                    int targetId;
                    try {
                        targetId = Integer.parseInt(idInput);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format. Operation canceled.");
                        break;
                    }

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
                case 4 -> {
                    System.out.println("Exiting Admin Menu...");
                    return;
                }
                default -> System.out.println("Invalid option. Choose between 1 and 4.");
            }
        }
    }

    // Formatted Console Output using standard string formatters (%-5s, %-15s, %-20s, %-10s)
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