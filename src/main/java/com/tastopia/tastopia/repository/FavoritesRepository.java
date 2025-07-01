package com.tastopia.tastopia.repository;

import com.tastopia.tastopia.entity.Favorites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritesRepository extends JpaRepository<Favorites, Long> {
    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);
    List<Favorites> findByUserId(Long userId);
    Optional<Favorites> findByUserIdAndRestaurantId(Long userId, Long restaurantId);
    void deleteByUserIdAndRestaurantId(Long userId, Long restaurantId);
}