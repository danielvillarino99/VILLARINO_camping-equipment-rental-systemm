package com.scc.model;

public record EquipmentItem(
        int item_id,
        String item_name,
        String category,
        double daily_rental_rate,
        int total_stock,
        String availability_status, // e.g., In Stock, Rented, In Maintenance, Out of Stock
        int condition_rating        // e.g., 1 to 5
) {}