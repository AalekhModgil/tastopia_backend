package com.tastopia.tastopia.repository;

import com.tastopia.tastopia.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {
    @Query("SELECT COUNT(b) > 0 FROM BlacklistedToken b WHERE LOWER(b.token) = LOWER(?1)")
    boolean existsByToken(String token);

    @Query("SELECT COUNT(b) FROM BlacklistedToken b WHERE LOWER(b.token) = LOWER(?1)")
    long countByToken(String token);

}