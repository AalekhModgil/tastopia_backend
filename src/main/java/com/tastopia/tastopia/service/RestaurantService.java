package com.tastopia.tastopia.service;

import com.tastopia.tastopia.dto.MenuItemRequest;
import com.tastopia.tastopia.dto.MenuItemResponse;
import com.tastopia.tastopia.dto.RestaurantRequest;
import com.tastopia.tastopia.dto.RestaurantResponse;
import com.tastopia.tastopia.entity.MenuItem;
import com.tastopia.tastopia.entity.Restaurant;
import com.tastopia.tastopia.exception.DuplicateRestaurantNameException;
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
                .ifPresent(restaurant -> {
                    throw new DuplicateRestaurantNameException("Restaurant name already taken");
                });

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setCuisine(request.getCuisine());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        RestaurantResponse response = new RestaurantResponse();
        response.setId(savedRestaurant.getId());
        response.setName(savedRestaurant.getName());
        response.setAddress(savedRestaurant.getAddress());
        response.setCuisine(savedRestaurant.getCuisine());
        return response;
    }

    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setPrice(request.getPrice());
        menuItem.setRestaurant(restaurant);

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        MenuItemResponse response = new MenuItemResponse();
        response.setId(savedMenuItem.getId());
        response.setName(savedMenuItem.getName());
        response.setPrice(savedMenuItem.getPrice());
        response.setRestaurantId(savedMenuItem.getRestaurant().getId());
        return response;
    }

    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(restaurant -> {
                    RestaurantResponse response = new RestaurantResponse();
                    response.setId(restaurant.getId());
                    response.setName(restaurant.getName());
                    response.setAddress(restaurant.getAddress());
                    response.setCuisine(restaurant.getCuisine());
                    return response;
                })
                .collect(Collectors.toList());
    }
}