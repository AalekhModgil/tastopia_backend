package com.tastopia.tastopia.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RestaurantResponse {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String contactNumber;
    private String cuisine;
    private String imageUrl;
    private boolean isOpen;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer avgDeliveryTimeInMinutes; // Added for Fast Delivery filter
    private Double averageRating; // Added for Top Rated filter
}