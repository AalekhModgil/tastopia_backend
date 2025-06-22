package com.tastopia.tastopia.dto;

import com.tastopia.tastopia.entity.MenuItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MenuItemResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private MenuItem.Category category;
    private boolean isAvailable;
    private MenuItem.VegStatus vegStatus;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}