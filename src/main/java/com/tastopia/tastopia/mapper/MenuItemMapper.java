package com.tastopia.tastopia.mapper;

import com.tastopia.tastopia.dto.MenuItemResponse;
import com.tastopia.tastopia.entity.MenuItem;
import org.springframework.stereotype.Component;

@Component
public class MenuItemMapper {

    public MenuItemResponse toMenuItemResponse(MenuItem menuItem) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(menuItem.getId());
        response.setName(menuItem.getName());
        response.setPrice(menuItem.getPrice());
        response.setDescription(menuItem.getDescription());
        response.setImageUrl(menuItem.getImageUrl());
        response.setCategory(menuItem.getCategory());
        response.setAvailable(menuItem.isAvailable());
        response.setVegStatus(menuItem.getVegStatus());
        response.setRestaurantId(menuItem.getRestaurant() != null ? menuItem.getRestaurant().getId() : null);
        response.setCreatedAt(menuItem.getCreatedAt());
        response.setUpdatedAt(menuItem.getUpdatedAt());
        return response;
    }
}