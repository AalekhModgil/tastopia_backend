package com.tastopia.tastopia.dto;

import lombok.Data;

@Data
public class MenuItemResponse {

    private Long id;
    private String name;
    private Double price;
    private Long restaurantId;
}