package com.scc.dao;

import com.scc.model.EquipmentItem;
import com.scc.model.RentalOrder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDao {
    private final ActivityLogDao logDao = new ActivityLogDao();

    // --- CREATE OPERATIONS ---
    public boolean registerEquipment(int clerkId, String name, String category, double rate, int stock, int condition) {
        String sql = "INSERT INTO equipment_inventory (item_name, category, daily_rental_rate, total_stock, availability_status, condition_rating) VALUES (?, ?, ?, ?, 'In Stock', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, rate);
            pstmt.setInt(4, stock);
            pstmt.setInt(5, condition);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                logDao.logAction(clerkId, "ITEM_REGISTERED", "Registered new gear item: " + name + " (" + category + ")");
            }
            return success;
        } catch (SQLException e) {
            System.out.println("Error registering equipment: " + e.getMessage());
            return false;
        }
    }

    public boolean createRentalOrder(int clerkId, int customerId, String customerName, int itemId, String itemName, int days, double totalAmount) {
        String sql = "INSERT INTO rental_orders (customer_id, customer_name, item_id, item_name, rental_duration_days, total_amount, order_status, penalty_fee) VALUES (?, ?, ?, ?, ?, ?, 'Active/Rented', 0.0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setString(2, customerName);
            pstmt.setInt(3, itemId);
            pstmt.setString(4, itemName);
            pstmt.setInt(5, days);
            pstmt.setDouble(6, totalAmount);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                logDao.logAction(clerkId, "ORDER_CREATED", "Created rental order for customer " + customerName + " (Item: " + itemName + ")");
            }
            return success;
        } catch (SQLException e) {
            System.out.println("Error creating rental order: " + e.getMessage());
            return false;
        }
    }

    // --- READ / SEARCH OPERATIONS ---
    public List<EquipmentItem> searchInventory(String categoryQuery, String statusQuery) {
        List<EquipmentItem> items = new ArrayList<>();
        String sql = "SELECT * FROM equipment_inventory WHERE (? = '' OR category LIKE ?) AND (? = '' OR availability_status = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryQuery);
            pstmt.setString(2, "%" + categoryQuery + "%");
            pstmt.setString(3, statusQuery);
            pstmt.setString(4, statusQuery);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new EquipmentItem(
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getString("category"),
                            rs.getDouble("daily_rental_rate"),
                            rs.getInt("total_stock"),
                            rs.getString("availability_status"),
                            rs.getInt("condition_rating")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching inventory: " + e.getMessage());
        }
        return items;
    }

    public List<EquipmentItem> getAllInventory() {
        List<EquipmentItem> items = new ArrayList<>();
        String sql = "SELECT * FROM equipment_inventory";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new EquipmentItem(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getDouble("daily_rental_rate"),
                        rs.getInt("total_stock"),
                        rs.getString("availability_status"),
                        rs.getInt("condition_rating")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching inventory: " + e.getMessage());
        }
        return items;
    }

    public List<RentalOrder> searchRentalOrders(String query) {
        List<RentalOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM rental_orders WHERE CAST(rental_id AS TEXT) = ? OR customer_name LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, query);
            pstmt.setString(2, "%" + query + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(new RentalOrder(
                            rs.getInt("rental_id"),
                            rs.getInt("customer_id"),
                            rs.getString("customer_name"),
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getInt("rental_duration_days"),
                            rs.getDouble("total_amount"),
                            rs.getString("order_status"),
                            rs.getDouble("penalty_fee")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching rental records: " + e.getMessage());
        }
        return orders;
    }

    public List<RentalOrder> getAllRentalOrders() {
        List<RentalOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM rental_orders";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(new RentalOrder(
                        rs.getInt("rental_id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getInt("rental_duration_days"),
                        rs.getDouble("total_amount"),
                        rs.getString("order_status"),
                        rs.getDouble("penalty_fee")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching rental orders: " + e.getMessage());
        }
        return orders;
    }

    // --- UPDATE OPERATIONS ---
    public boolean updateEquipment(int clerkId, int itemId, int stock, double rate, String status) {
        String sql = "UPDATE equipment_inventory SET total_stock = ?, daily_rental_rate = ?, availability_status = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, stock);
            pstmt.setDouble(2, rate);
            pstmt.setString(3, status);
            pstmt.setInt(4, itemId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                logDao.logAction(clerkId, "ITEM_UPDATED", "Updated equipment ID " + itemId + " (Stock: " + stock + ", Status: " + status + ")");
            }
            return success;
        } catch (SQLException e) {
            System.out.println("Error updating equipment: " + e.getMessage());
            return false;
        }
    }

    public boolean updateRentalStatus(int clerkId, int rentalId, String newStatus, double penaltyFee) {
        String sql = "UPDATE rental_orders SET order_status = ?, penalty_fee = ? WHERE rental_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setDouble(2, penaltyFee);
            pstmt.setInt(3, rentalId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                logDao.logAction(clerkId, "ORDER_UPDATED", "Updated rental ID " + rentalId + " to status: " + newStatus + (penaltyFee > 0 ? " with penalty: $" + penaltyFee : ""));
            }
            return success;
        } catch (SQLException e) {
            System.out.println("Error updating rental order: " + e.getMessage());
            return false;
        }
    }

    // --- DELETE OPERATIONS ---
    public boolean deleteRentalOrder(int clerkId, int rentalId) {
        String sql = "DELETE FROM rental_orders WHERE rental_id = ? AND order_status = 'Active/Rented'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rentalId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                logDao.logAction(clerkId, "ORDER_DELETED", "Removed unverified/canceled rental order ID " + rentalId);
            }
            return success;
        } catch (SQLException e) {
            System.out.println("Error deleting rental order: " + e.getMessage());
            return false;
        }
    }

    public boolean softDeleteEquipment(int clerkId, int itemId) {
        String sql = "UPDATE equipment_inventory SET availability_status = 'Damaged/Lost (Archived)' WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            boolean success = pstmt.executeUpdate() > 0;
            if (success) {
                logDao.logAction(clerkId, "ITEM_DELETED", "Soft-deleted/archived damaged or lost item ID " + itemId);
            }
            return success;
        } catch (SQLException e) {
            System.out.println("Error soft-deleting equipment: " + e.getMessage());
            return false;
        }
    }
}