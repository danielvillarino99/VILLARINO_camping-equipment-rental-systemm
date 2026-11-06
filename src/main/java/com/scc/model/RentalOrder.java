package com.scc.model;

public record RentalOrder(
        int rental_id,
        int customer_id,
        String customer_name,
        int item_id,
        String item_name,
        int rental_duration_days,
        double total_amount,
        String order_status, // e.g., Active/Rented, Returned & Paid, Canceled
        double penalty_fee
) {}