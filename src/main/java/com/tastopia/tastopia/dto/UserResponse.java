package com.tastopia.tastopia.dto;

import com.tastopia.tastopia.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
    private User.Role role;
    private boolean IsActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}