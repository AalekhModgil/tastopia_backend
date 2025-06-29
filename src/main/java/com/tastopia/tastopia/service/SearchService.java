package com.tastopia.tastopia.service;

import com.tastopia.tastopia.entity.MenuItem;
import com.tastopia.tastopia.entity.Restaurant;
import com.tastopia.tastopia.exception.ResourceNotFoundException;
import com.tastopia.tastopia.mapper.MenuItemMapper;
import com.tastopia.tastopia.mapper.RestaurantMapper;
import com.tastopia.tastopia.repository.MenuItemRepository;
import com.tastopia.tastopia.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantMapper restaurantMapper;
    private final MenuItemMapper menuItemMapper;

    public Map<String, Object> search(String query, Integer page, Integer size) {
        if (query == null || query.trim().isEmpty()) {
            throw new ResourceNotFoundException("Query parameter is required");
        }

        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 10);
        String searchQuery = "%" + query.toLowerCase() + "%";

        Page<Restaurant> restaurantPage = restaurantRepository.findByNameContainingIgnoreCase(searchQuery, pageable);
        Page<MenuItem> menuItemPage = menuItemRepository.findByNameOrCategoryContainingIgnoreCase(searchQuery, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("restaurants", restaurantMapper.toRestaurantResponseList(restaurantPage.getContent()));
        response.put("menuItems", menuItemMapper.toMenuItemResponseList(menuItemPage.getContent()));
        response.put("totalRestaurants", restaurantPage.getTotalElements());
        response.put("totalMenuItems", menuItemPage.getTotalElements());
        response.put("page", page != null ? page : 0);
        response.put("size", size != null ? size : 10);
        response.put("totalPages", Math.max(restaurantPage.getTotalPages(), menuItemPage.getTotalPages()));

        return response;
    }
}