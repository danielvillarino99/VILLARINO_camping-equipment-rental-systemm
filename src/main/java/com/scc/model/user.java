package com.scc.model;

public record user(int user_id, String username, String password_hash, String role, String status) {
}