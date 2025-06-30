package com.tastopia.tastopia.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "menu_item_ratings")
public class MenuItemRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private Integer rating; // 1 to 5

    @Column(length = 500)
    private String comment;
}