package com.tastopia.tastopia.controller;

import com.tastopia.tastopia.dto.MenuItemRequest;
import com.tastopia.tastopia.dto.MenuItemResponse;
import com.tastopia.tastopia.dto.RestaurantRequest;
import com.tastopia.tastopia.dto.RestaurantResponse;
import com.tastopia.tastopia.entity.MenuItem;
import com.tastopia.tastopia.entity.Restaurant;
import com.tastopia.tastopia.mapper.MenuItemMapper;
import com.tastopia.tastopia.mapper.RestaurantMapper;
import com.tastopia.tastopia.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuItemMapper menuItemMapper;
    private final RestaurantMapper restaurantMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        Restaurant restaurant = restaurantService.createRestaurant(request);
        return restaurantMapper.toRestaurantResponse(restaurant);
    }

    @PostMapping("/{restaurantId}/menu-items")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse createMenuItem(@PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        MenuItem menuItem = restaurantService.createMenuItem(restaurantId, request);
        return menuItemMapper.toMenuItemResponse(menuItem);
    }

    @GetMapping
    public List<RestaurantResponse> getAllRestaurants() {
       List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        return restaurantMapper.toRestaurantResponseList(restaurants);
    }
}