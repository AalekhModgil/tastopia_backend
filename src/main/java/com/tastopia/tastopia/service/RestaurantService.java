package com.tastopia.tastopia.service;

import com.tastopia.tastopia.dto.MenuItemRequest;
import com.tastopia.tastopia.dto.RestaurantRequest;
import com.tastopia.tastopia.entity.MenuItem;
import com.tastopia.tastopia.entity.Restaurant;
import com.tastopia.tastopia.exception.DuplicateRestaurantNameException;
import com.tastopia.tastopia.exception.ResourceNotFoundException;
import com.tastopia.tastopia.repository.MenuItemRepository;
import com.tastopia.tastopia.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public Restaurant createRestaurant(RestaurantRequest request) {
        restaurantRepository.findByName(request.getName())
            .ifPresent(restaurant -> { throw new DuplicateRestaurantNameException("Restaurant name already taken"); });

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setState(request.getState());
        restaurant.setContactNumber(request.getContactNumber());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setOpen(true);

        return  restaurantRepository.save(restaurant);
    }

    public MenuItem createMenuItem(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice());
        menuItem.setDescription(request.getDescription());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setCategory(request.getCategory());
        menuItem.setAvailable(true);
        menuItem.setVegStatus(request.getVegStatus());
        menuItem.setRestaurant(restaurant);

        return menuItemRepository.save(menuItem);
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }
}