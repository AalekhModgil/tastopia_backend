package com.tastopia.tastopia.repository;

import com.tastopia.tastopia.entity.MenuItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    @Query("SELECT m FROM MenuItem m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(CAST(m.category AS string)) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<MenuItem> findByNameOrCategoryContainingIgnoreCase(@Param("query") String query, Pageable pageable);
}