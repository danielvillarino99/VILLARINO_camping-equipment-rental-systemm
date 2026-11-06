package com.scc.ui;

import com.scc.dao.InventoryDao;
import com.scc.model.EquipmentItem;
import com.scc.model.RentalOrder;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class RentalDeskClerkMenu {
    private static final InventoryDao inventoryDao = new InventoryDao();

    public static void displayClerkMenu(user currentUser, Scanner scanner) {
        while (true) {
            System.out.println("\n=== RENTAL DESK CLERK DASHBOARD ===");
            System.out.println("[1] Register New Equipment Item");
            System.out.println("[2] Create Customer Rental Order");
            System.out.println("[3] Search Active Gear Inventory");
            System.out.println("[4] Search Customer Rental Records");
            System.out.println("[5] Update Equipment Stock/Rate/Status");
            System.out.println("[6] Update Rental Payment & Return Status (Add Penalty)");
            System.out.println("[7] Remove Unverified/Canceled Rental Order");
            System.out.println("[8] Soft-Delete Damaged/Lost Equipment");
            System.out.println("[9] Logout / Exit Session");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    System.out.println("\n--- REGISTER NEW EQUIPMENT ---");
                    System.out.print("Enter item name (e.g., Camping Tent, Backpack): ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Enter category (e.g., Tents, Backpacks, Stoves): ");
                    String category = scanner.nextLine().trim();
                    double rate = readDoubleInput(scanner, "Enter daily rental rate ($): ");
                    int stock = readIntInput(scanner, "Enter total stock quantity: ");
                    int condition = readIntInput(scanner, "Enter condition rating (1-5): ");

                    boolean success = inventoryDao.registerEquipment(currentUser.user_id(), name, category, rate, stock, condition);
                    if (success) {
                        System.out.println("SUCCESS: Equipment item registered successfully!");
                    } else {
                        System.out.println("Failed to register equipment.");
                    }
                }
                case 2 -> {
                    System.out.println("\n--- CREATE RENTAL ORDER ---");
                    List<EquipmentItem> items = inventoryDao.getAllInventory();
                    printInventoryTable("AVAILABLE INVENTORY", items);

                    int itemId = readIntInput(scanner, "Enter Item ID to rent: ");
                    EquipmentItem selectedItem = items.stream().filter(i -> i.item_id() == itemId).findFirst().orElse(null);

                    if (selectedItem == null) {
                        System.out.println("Invalid Item ID.");
                        break;
                    }

                    int customerId = readIntInput(scanner, "Enter Customer ID: ");
                    System.out.print("Enter Customer Name: ");
                    String customerName = scanner.nextLine().trim();
                    int days = readIntInput(scanner, "Enter rental duration (days): ");

                    double totalAmount = selectedItem.daily_rental_rate() * days;
                    System.out.println("\n--- ITEMIZED INVOICE ---");
                    System.out.println("Customer: " + customerName);
                    System.out.println("Item: " + selectedItem.item_name());
                    System.out.println("Daily Rate: $" + selectedItem.daily_rental_rate());
                    System.out.println("Duration: " + days + " days");
                    System.out.println("Total Amount Due: $" + totalAmount);
                    System.out.println("-------------------------");

                    System.out.print("Confirm order creation? (y/n): ");
                    String confirm = scanner.nextLine().trim();
                    if (confirm.equalsIgnoreCase("y")) {
                        boolean created = inventoryDao.createRentalOrder(currentUser.user_id(), customerId, customerName, itemId, selectedItem.item_name(), days, totalAmount);
                        if (created) {
                            System.out.println("SUCCESS: Rental order created and invoice generated!");
                        } else {
                            System.out.println("Failed to create rental order.");
                        }
                    } else {
                        System.out.println("Rental order cancelled.");
                    }
                }
                case 3 -> {
                    System.out.println("\n--- SEARCH INVENTORY ---");
                    System.out.print("Filter by category (leave blank for all): ");
                    String cat = scanner.nextLine().trim();
                    System.out.print("Filter by status (In Stock, Rented, In Maintenance, leave blank for all): ");
                    String status = scanner.nextLine().trim();

                    List<EquipmentItem> results = inventoryDao.searchInventory(cat, status);
                    printInventoryTable("GEAR INVENTORY SEARCH RESULTS", results);
                }
                case 4 -> {
                    System.out.println("\n--- SEARCH RENTAL RECORDS ---");
                    System.out.print("Enter Rental ID or Customer Name search query: ");
                    String query = scanner.nextLine().trim();

                    List<RentalOrder> results = inventoryDao.searchRentalOrders(query);
                    printRentalTable("RENTAL RECORDS SEARCH RESULTS", results);
                }
                case 5 -> {
                    System.out.println("\n--- UPDATE EQUIPMENT ---");
                    List<EquipmentItem> items = inventoryDao.getAllInventory();
                    printInventoryTable("ALL INVENTORY", items);

                    int itemId = readIntInput(scanner, "Enter Item ID to update: ");
                    int newStock = readIntInput(scanner, "Enter updated total stock: ");
                    double newRate = readDoubleInput(scanner, "Enter updated daily rental rate: ");
                    System.out.print("Enter availability status (In Stock, Rented, In Maintenance): ");
                    String newStatus = scanner.nextLine().trim();

                    boolean success = inventoryDao.updateEquipment(currentUser.user_id(), itemId, newStock, newRate, newStatus);
                    if (success) {
                        System.out.println("SUCCESS: Equipment updated successfully!");
                    } else {
                        System.out.println("Failed to update equipment.");
                    }
                }
                case 6 -> {
                    System.out.println("\n--- UPDATE RENTAL & RETURN STATUS ---");
                    List<RentalOrder> orders = inventoryDao.getAllRentalOrders();
                    printRentalTable("ALL RENTAL ORDERS", orders);

                    int rentalId = readIntInput(scanner, "Enter Rental ID to update: ");
                    System.out.print("Enter new status (e.g., Returned & Paid, Active/Rented): ");
                    String newStatus = scanner.nextLine().trim();
                    double penalty = readDoubleInput(scanner, "Enter damaged item penalty charge ($0 if none): ");

                    boolean success = inventoryDao.updateRentalStatus(currentUser.user_id(), rentalId, newStatus, penalty);
                    if (success) {
                        System.out.println("SUCCESS: Rental status and penalty updated!");
                    } else {
                        System.out.println("Failed to update rental status.");
                    }
                }
                case 7 -> {
                    System.out.println("\n--- REMOVE UNVERIFIED/CANCELED ORDER ---");
                    int rentalId = readIntInput(scanner, "Enter Rental ID of canceled/unverified order to delete: ");
                    boolean success = inventoryDao.deleteRentalOrder(currentUser.user_id(), rentalId);
                    if (success) {
                        System.out.println("SUCCESS: Canceled rental order entry removed.");
                    } else {
                        System.out.println("Failed to remove order. Ensure it is an active/unverified entry.");
                    }
                }
                case 8 -> {
                    System.out.println("\n--- SOFT-DELETE DAMAGED/LOST EQUIPMENT ---");
                    List<EquipmentItem> items = inventoryDao.getAllInventory();
                    printInventoryTable("ALL INVENTORY", items);

                    int itemId = readIntInput(scanner, "Enter Item ID of permanently damaged or lost equipment: ");
                    boolean success = inventoryDao.softDeleteEquipment(currentUser.user_id(), itemId);
                    if (success) {
                        System.out.println("SUCCESS: Item archived/soft-deleted while retaining audit compliance records.");
                    } else {
                        System.out.println("Failed to soft-delete equipment.");
                    }
                }
                case 9 -> {
                    System.out.println("Exiting Rental Desk Clerk Dashboard...");
                    return;
                }
                default -> System.out.println("Invalid option. Please choose between 1 and 9.");
            }
        }
    }

    private static int readIntInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private static double readDoubleInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please try again.");
            }
        }
    }

    private static void printInventoryTable(String title, List<EquipmentItem> items) {
        System.out.println("\n--- " + title + " ---");
        if (items.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        String border = "+-----+-------------------+-----------------+-------+-------+--------------------+-----------+";
        System.out.println(border);
        System.out.printf("| %-3s | %-17s | %-15s | %-5s | %-5s | %-18s | %-9s |\n", "ID", "Name", "Category", "Rate", "Stock", "Status", "Condition");
        System.out.println(border);
        for (EquipmentItem i : items) {
            System.out.printf("| %-3d | %-17s | %-15s | $%-4.2f | %-5d | %-18s | %-9d |\n",
                    i.item_id(), i.item_name(), i.category(), i.daily_rental_rate(), i.total_stock(), i.availability_status(), i.condition_rating());
        }
        System.out.println(border);
    }

    private static void printRentalTable(String title, List<RentalOrder> orders) {
        System.out.println("\n--- " + title + " ---");
        if (orders.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        String border = "+-----------+-------------+-------------------+---------+-------------------+----------+-------------+------------------+------------+";
        System.out.println(border);
        System.out.printf("| %-9s | %-11s | %-17s | %-7s | %-17s | %-8s | %-11s | %-16s | %-10s |\n",
                "Rental ID", "Cust ID", "Customer Name", "Item ID", "Item Name", "Days", "Total Amt", "Order Status", "Penalty");
        System.out.println(border);
        for (RentalOrder r : orders) {
            System.out.printf("| %-9d | %-11d | %-17s | %-7d | %-17s | %-8d | $%-10.2f | %-16s | $%-9.2f |\n",
                    r.rental_id(), r.customer_id(), r.customer_name(), r.item_id(), r.item_name(), r.rental_duration_days(), r.total_amount(), r.order_status(), r.penalty_fee());
        }
        System.out.println(border);
    }
}