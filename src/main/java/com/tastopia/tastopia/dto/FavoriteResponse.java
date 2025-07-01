package com.tastopia.tastopia.dto;

import lombok.Data;

@Data
public class FavoriteResponse {
    private Long restaurantId;
    private String restaurantName;
    private String imageUrl;
}