package com.tastopia.tastopia.controller;

import com.tastopia.tastopia.dto.MenuItemRequest;
import com.tastopia.tastopia.dto.MenuItemResponse;
import com.tastopia.tastopia.dto.RestaurantRequest;
import com.tastopia.tastopia.dto.RestaurantResponse;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        return restaurantService.createRestaurant(request);
    }

    @PostMapping("/{restaurantId}/menu-items")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse createMenuItem(@PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        return restaurantService.createMenuItem(restaurantId, request);
    }

    @GetMapping
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }
}