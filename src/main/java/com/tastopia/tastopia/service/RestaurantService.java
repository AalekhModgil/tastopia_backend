package com.tastopia.tastopia.service;

import com.tastopia.tastopia.dto.MenuItemRequest;
import com.tastopia.tastopia.dto.MenuItemResponse;
import com.tastopia.tastopia.dto.RestaurantRequest;
import com.tastopia.tastopia.dto.RestaurantResponse;
import com.tastopia.tastopia.entity.MenuItem;
import com.tastopia.tastopia.entity.Restaurant;
import com.tastopia.tastopia.exception.DuplicateRestaurantNameException;
import com.tastopia.tastopia.exception.ResourceNotFoundException;
import com.tastopia.tastopia.repository.MenuItemRepository;
import com.tastopia.tastopia.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantResponse createRestaurant(RestaurantRequest request) {
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

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        RestaurantResponse response = new RestaurantResponse();
        response.setId(savedRestaurant.getId());
        response.setName(savedRestaurant.getName());
        response.setAddress(savedRestaurant.getAddress());
        response.setCity(savedRestaurant.getCity());
        response.setState(savedRestaurant.getState());
        response.setContactNumber(savedRestaurant.getContactNumber());
        response.setCuisine(savedRestaurant.getCuisine());
        response.setImageUrl(savedRestaurant.getImageUrl());
        response.setOpen(savedRestaurant.isOpen());
        response.setCreatedAt(savedRestaurant.getCreatedAt());
        response.setUpdatedAt(savedRestaurant.getUpdatedAt());
        return response;
    }

    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request) {
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

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        MenuItemResponse response = new MenuItemResponse();
        response.setId(savedMenuItem.getId());
        response.setName(savedMenuItem.getName());
        response.setPrice(savedMenuItem.getPrice());
        response.setDescription(savedMenuItem.getDescription());
        response.setImageUrl(savedMenuItem.getImageUrl());
        response.setCategory(savedMenuItem.getCategory());
        response.setAvailable(savedMenuItem.isAvailable());
        response.setVegStatus(savedMenuItem.getVegStatus());
        response.setRestaurantId(savedMenuItem.getRestaurant().getId());
        response.setCreatedAt(savedMenuItem.getCreatedAt());
        response.setUpdatedAt(savedMenuItem.getUpdatedAt());
        return response;
    }

    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
            .map(restaurant -> {
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
                return response;
            })
            .collect(Collectors.toList());
    }
}