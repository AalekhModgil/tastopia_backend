package com.tastopia.tastopia.controller;

import com.tastopia.tastopia.service.RestaurantFilterService;
import com.tastopia.tastopia.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final RestaurantFilterService filterService;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String query,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Map<String, Object> response = searchService.search(query, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurants/filter")
    public ResponseEntity<Map<String, Object>> filterRestaurants(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Boolean vegOnly,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean fastDelivery) {
        Map<String, Object> response = filterService.filterRestaurants(page, size, vegOnly, minRating, fastDelivery);
        return ResponseEntity.ok(response);
    }
}