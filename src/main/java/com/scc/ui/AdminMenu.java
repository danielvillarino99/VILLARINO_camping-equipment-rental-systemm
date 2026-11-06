package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private static final ActivityLogDao activityLogDao = new ActivityLogDao();

    public static void displayAdminMenu(user currentAdmin, UserDao userDaoObj, Scanner scanner) {
        while (true) {
            System.out.println("\n===================================");
            System.out.println("=== SYSTEM ADMINISTRATOR MENU ===");
            System.out.println("=====================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending Users");
            System.out.println("3. Approve User");
            System.out.println("4. Update User Role & Status");
            System.out.println("5. View System Audit Logs");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");

            int choice = readIntInput(scanner, "");

            switch (choice) {
                case 1 -> displayAllUsers(userDaoObj);
                case 2 -> displayPendingUsers(userDaoObj);
                case 3 -> handleApproveUser(currentAdmin, userDaoObj, scanner);
                case 4 -> handleUpdateUser(currentAdmin, userDaoObj, scanner);
                case 5 -> handleViewAuditLogsMenu(scanner);
                case 6 -> {
                    System.out.println("Logging out from Admin session.");
                    return;
                }
                default -> System.out.println("Invalid choice. Please choose between 1 and 6.");
            }
        }
    }

    private static int readIntInput(Scanner scanner, String prompt) {
        while (true) {
            if (!prompt.isEmpty()) {
                System.out.print(prompt);
            }
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private static String readValidRole(Scanner scanner) {
        while (true) {
            System.out.print("Enter new role (System Admin, Rental Desk Clerk, Inventory Auditor) [0 to cancel]: ");
            String role = scanner.nextLine().trim();
            if (role.equals("0")) return null;

            if (!role.isEmpty()) {
                return role.toUpperCase();
            }
            System.out.println("Role cannot be empty. Please try again.");
        }
    }

    private static String readValidStatus(Scanner scanner) {
        while (true) {
            System.out.print("Enter new status (APPROVED / PENDING) [0 to cancel]: ");
            String status = scanner.nextLine().trim();
            if (status.equals("0")) return null;

            if (status.equalsIgnoreCase("APPROVED") || status.equalsIgnoreCase("PENDING")) {
                return status.toUpperCase();
            }
            System.out.println("Invalid status. Please enter APPROVED or PENDING.");
        }
    }

    private static void displayAllUsers(UserDao userDao) {
        List<user> users = userDao.getAllUsers();
        System.out.println("\n==================================================================");
        System.out.println("====================== ALL SYSTEM USERS ==========================");
        System.out.println("==================================================================");
        System.out.printf("%-5s | %-20s | %-25s | %-10s%n", "ID", "Username", "Role", "Status");
        System.out.println("------------------------------------------------------------------");
        for (user u : users) {
            System.out.printf("%-5d | %-20s | %-25s | %-10s%n",
                    u.user_id(), u.username(), u.role(), u.status());
        }
        System.out.println("------------------------------------------------------------------");
    }

    private static void displayPendingUsers(UserDao userDao) {
        List<user> users = userDao.getPendingUsers();
        System.out.println("\n------------------------------------------------------------------");
        System.out.println("------------------------- PENDING USERS --------------------------");
        System.out.println("------------------------------------------------------------------");
        if (users.isEmpty()) {
            System.out.println("No pending users found.");
            System.out.println("------------------------------------------------------------------");
            return;
        }
        System.out.printf("%-5s | %-20s | %-25s | %-10s%n", "No.", "Username", "Role", "Status");
        System.out.println("------------------------------------------------------------------");
        int displayNo = 1;
        for (user u : users) {
            System.out.printf("%-5d | %-20s | %-25s | %-10s%n",
                    displayNo++, u.username(), u.role(), u.status());
        }
        System.out.println("------------------------------------------------------------------");
    }

    private static void handleApproveUser(user currentAdmin, UserDao userDao, Scanner scanner) {
        List<user> pending = userDao.getPendingUsers();
        System.out.println("\n------------------------------------------------------------------");
        System.out.println("------------------------- PENDING USERS --------------------------");
        System.out.println("------------------------------------------------------------------");
        if (pending.isEmpty()) {
            System.out.println("No pending users found.");
            System.out.println("------------------------------------------------------------------");
            return;
        }

        System.out.printf("%-5s | %-20s | %-25s | %-10s%n", "No.", "Username", "Role", "Status");
        System.out.println("------------------------------------------------------------------");
        int displayNo = 1;
        for (user u : pending) {
            System.out.printf("%-5d | %-20s | %-25s | %-10s%n",
                    displayNo++, u.username(), u.role(), u.status());
        }
        System.out.println("------------------------------------------------------------------");

        int selection = readIntInput(scanner, "Enter number to approve (or 0 to cancel): ");
        if (selection == 0) return;

        if (selection < 1 || selection > pending.size()) {
            System.out.println("Invalid selection number.");
            return;
        }

        user targetUser = pending.get(selection - 1);
        boolean success = userDao.approveUser(currentAdmin.user_id(), targetUser.user_id());

        if (success) {
            System.out.println("User '" + targetUser.username() + "' successfully approved.");
        } else {
            System.out.println("Approval failed.");
        }
    }

    private static void handleUpdateUser(user currentAdmin, UserDao userDao, Scanner scanner) {
        displayAllUsers(userDao);

        int targetUserId = readIntInput(scanner, "\nEnter User ID to update (or 0 to cancel): ");
        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        if (targetUserId == currentAdmin.user_id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        String newRole = readValidRole(scanner);
        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus(scanner);
        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        boolean success = userDao.updateUserRoleAndStatus(currentAdmin.user_id(), targetUserId, newRole, newStatus);
        if (success) {
            System.out.println("User ID " + targetUserId + " updated successfully to Role: " + newRole + ", Status: " + newStatus);
        } else {
            System.out.println("Failed to update user. Please check if the User ID exists.");
        }
    }

    private static void handleViewAuditLogsMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=================================");
            System.out.println("=== AUDIT LOGS MANAGEMENT ===");
            System.out.println("=================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Back to Admin Menu");
            System.out.print("Choose an option: ");

            int choice = readIntInput(scanner, "");
            switch (choice) {
                case 1 -> displayAuditLogs(activityLogDao.getAllLogs());
                case 2 -> {
                    int filterUserId = readIntInput(scanner, "Enter User ID to filter by: ");
                    displayAuditLogs(activityLogDao.getLogsByUserId(filterUserId));
                }
                case 3 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please choose between 1 and 3.");
            }
        }
    }

    private static void displayAuditLogs(List<ActivityLog> logs) {
        System.out.println("\n=======================================================================================================================");
        System.out.println("================================================ SYSTEM AUDIT LOGS ====================================================");
        System.out.println("=======================================================================================================================");
        System.out.printf("%-5s | %-8s | %-18s | %-55s | %-20s%n", "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        if (logs.isEmpty()) {
            System.out.println("No audit logs found.");
        } else {
            for (ActivityLog log : logs) {
                System.out.printf("%-5s | %-8s | %-18s | %-55s | %-20s%n",
                        log.log_id(), log.user_id(), log.action_type(), log.description(), log.timestamp());
            }
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        System.out.println("Total Audit Logs Recorded: " + logs.size());
        System.out.println("=======================================================================================================================");
    }
}