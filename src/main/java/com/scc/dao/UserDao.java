package com.scc.dao;

import com.scc.model.user;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final ActivityLogDao logDao = new ActivityLogDao();

    public boolean registerUser(String username, String password, String role) {
        String status = "System Admin".equalsIgnoreCase(role) ? "APPROVED" : "PENDING";
        String sql = "INSERT INTO users(username, password_hash, role, status) VALUES(?, ?, ?, ?)";
        int newUserId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.setString(4, status);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newUserId = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Registration DB Error: " + e.getMessage());
            return false;
        }

        // Log action outside connection block to prevent database locking
        if (newUserId != -1) {
            logDao.logAction(newUserId, "USER_REGISTERED", "User registered with role: " + role);
            return true;
        }
        return false;
    }

    public user loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        user loggedInUser = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    loggedInUser = new user(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Login DB Error: " + e.getMessage());
        }

        // Log action AFTER closing the login SELECT connection
        if (loggedInUser != null) {
            logDao.logAction(loggedInUser.user_id(), "USER_LOGIN", "User logged in successfully.");
        }

        return loggedInUser;
    }

    public List<user> getAllUsers() {
        List<user> usersList = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                usersList.add(new user(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching all users: " + e.getMessage());
        }
        return usersList;
    }

    public List<user> getPendingUsers() {
        List<user> pendingList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pendingList.add(new user(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching pending users: " + e.getMessage());
        }
        return pendingList;
    }

    public boolean approveUser(int adminId, int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE user_id = ?";
        boolean success = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            success = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error approving user: " + e.getMessage());
        }

        if (success) {
            logDao.logAction(adminId, "USER_APPROVED", "Admin ID " + adminId + " approved account for User ID " + userId);
        }
        return success;
    }

    public boolean updateUserRoleAndStatus(int adminId, int userId, String newRole, String newStatus) {
        String sql = "UPDATE users SET role = ?, status = ? WHERE user_id = ?";
        boolean success = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole.toUpperCase());
            pstmt.setString(2, newStatus.toUpperCase());
            pstmt.setInt(3, userId);
            success = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating user role and status: " + e.getMessage());
        }

        if (success) {
            logDao.logAction(adminId, "USER_UPDATED", "Admin ID " + adminId + " changed role/status of User ID " + userId + " to Role: " + newRole + ", Status: " + newStatus);
        }
        return success;
    }
}