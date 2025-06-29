package com.tastopia.tastopia.service;

import com.tastopia.tastopia.entity.MenuItem;
import com.tastopia.tastopia.entity.Restaurant;
import com.tastopia.tastopia.exception.ResourceNotFoundException;
import com.tastopia.tastopia.mapper.MenuItemMapper;
import com.tastopia.tastopia.mapper.RestaurantMapper;
import com.tastopia.tastopia.repository.MenuItemRepository;
import com.tastopia.tastopia.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        int adjustedPage = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = size != null ? size : 10;
        String searchQuery = query;

        // 🔁 Fetch all (not paged!)
        List<Restaurant> restaurantList = restaurantRepository.findByNameContainingIgnoreCase(searchQuery);
        List<MenuItem> menuItemList = menuItemRepository.findByNameOrCategoryContainingIgnoreCase(searchQuery);

        List<Map<String, Object>> allResults = new ArrayList<>();
        allResults.addAll(restaurantMapper.toRestaurantResponseList(restaurantList).stream()
                .map(r -> createResultMap("restaurant", r)).collect(Collectors.toList()));
        allResults.addAll(menuItemMapper.toMenuItemResponseList(menuItemList).stream()
                .map(m -> createResultMap("menuitem", m)).collect(Collectors.toList()));

        // 🧮 Paginate after combining
        long totalItems = allResults.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int start = adjustedPage * pageSize;
        int end = Math.min(start + pageSize, allResults.size());
        List<Map<String, Object>> paginatedResults = (start < allResults.size()) ? allResults.subList(start, end)
                : new ArrayList<>();

        Map<String, Object> response = new HashMap<>();
        response.put("results", paginatedResults);
        response.put("totalItems", totalItems);
        response.put("page", page != null ? page : 1);
        response.put("size", pageSize);
        response.put("totalPages", totalPages);

        return response;
    }

    private Map<String, Object> createResultMap(String type, Object item) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", type);
        result.put("item", item);
        return result;
    }
}