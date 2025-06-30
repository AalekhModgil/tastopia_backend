package com.tastopia.tastopia.dto;

import com.tastopia.tastopia.entity.MenuItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Description is required")
    private String description;

    private String imageUrl;

    @NotNull(message = "Category is required")
    private MenuItem.Category category;

    @NotNull(message = "Veg status is required")
    private MenuItem.VegStatus vegStatus;

    private boolean isAvailable; // Added for completeness
    private Long restaurantId; // Added for association
}