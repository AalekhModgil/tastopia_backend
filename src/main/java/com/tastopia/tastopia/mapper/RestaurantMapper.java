package com.tastopia.tastopia.mapper;

import com.tastopia.tastopia.dto.RestaurantResponse;
import com.tastopia.tastopia.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RestaurantMapper {

    public RestaurantResponse toRestaurantResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setAddress(restaurant.getAddress());
        response.setCity(restaurant.getCity());
        response.setState(restaurant.getState());
        response.setContactNumber(restaurant.getContactNumber());
        response.setCuisine(restaurant.getCuisine());
        response.setImageUrl(restaurant.getImageUrl());
        response.setOpen(restaurant.isOpen());
        response.setCreatedAt(restaurant.getCreatedAt());
        response.setUpdatedAt(restaurant.getUpdatedAt());
        response.setAvgDeliveryTimeInMinutes(restaurant.getAvgDeliveryTimeInMinutes());

        // Compute average rating (simplified; assumes ratings are accessible via menu items)
        if (restaurant.getMenuItems() != null && !restaurant.getMenuItems().isEmpty()) {
            double avgRating = restaurant.getMenuItems().stream()
                .flatMap(menuItem -> menuItem.getRatings().stream())
                .mapToInt(rating -> rating.getRating())
                .average()
                .orElse(0.0);
            response.setAverageRating(avgRating);
        } else {
            response.setAverageRating(0.0);
        }
        return response;
    }

    public List<RestaurantResponse> toRestaurantResponseList(List<Restaurant> restaurants) {
        return restaurants.stream()
            .map(this::toRestaurantResponse)
            .collect(Collectors.toList());
    }
}