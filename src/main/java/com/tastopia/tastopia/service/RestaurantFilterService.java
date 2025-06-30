package com.tastopia.tastopia.service;

import com.tastopia.tastopia.entity.Restaurant;
import com.tastopia.tastopia.exception.ResourceNotFoundException;
import com.tastopia.tastopia.mapper.RestaurantMapper;
import com.tastopia.tastopia.repository.RestaurantRepository;
import com.tastopia.tastopia.specification.RestaurantSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RestaurantFilterService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;

    public Map<String, Object> filterRestaurants(Integer page, Integer size, Boolean vegOnly, Double minRating, Boolean fastDelivery) {
        int adjustedPage = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (size != null && size > 0) ? size : 10;

        if (adjustedPage < 0) {
            throw new ResourceNotFoundException("Page number must be positive");
        }

        // Dynamic filtering with Specification
        Specification<Restaurant> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (vegOnly != null && vegOnly) {
            spec = spec.and(RestaurantSpecification.isVegOnly());
        }
        if (minRating != null) {
            spec = spec.and(RestaurantSpecification.hasMinimumRating(minRating));
        }
        if (fastDelivery != null && fastDelivery) {
            spec = spec.and(RestaurantSpecification.isFastDelivery());
        }

        // Fetch paginated results
        Page<Restaurant> restaurantPage = restaurantRepository.findAll(spec, PageRequest.of(adjustedPage, pageSize));
        Map<String, Object> response = new HashMap<>();
        response.put("results", restaurantMapper.toRestaurantResponseList(restaurantPage.getContent()));
        response.put("totalItems", restaurantPage.getTotalElements());
        response.put("page", page != null ? page : 1);
        response.put("size", pageSize);
        response.put("totalPages", restaurantPage.getTotalPages());

        return response;
    }
}