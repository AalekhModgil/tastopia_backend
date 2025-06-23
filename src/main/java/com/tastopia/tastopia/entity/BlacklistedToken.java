package com.tastopia.tastopia.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class BlacklistedToken {
    @Id
    private String token;
    private LocalDateTime expiryDate;
}