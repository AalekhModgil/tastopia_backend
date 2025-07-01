package com.tastopia.tastopia.controller;

import com.tastopia.tastopia.config.JwtConfig;
import com.tastopia.tastopia.dto.FavoriteCheckResponse;
import com.tastopia.tastopia.dto.FavoriteResponse;
import com.tastopia.tastopia.dto.LoginRequest;
import com.tastopia.tastopia.dto.UpdateUserRequest;
import com.tastopia.tastopia.dto.UserRequest;
import com.tastopia.tastopia.dto.UserResponse;
import com.tastopia.tastopia.entity.BlacklistedToken;
import com.tastopia.tastopia.entity.Favorites;
import com.tastopia.tastopia.entity.User;
import com.tastopia.tastopia.mapper.UserMapper;
import com.tastopia.tastopia.repository.BlacklistedTokenRepository;
import com.tastopia.tastopia.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserMapper userMapper;
    private final JwtConfig jwtConfig;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000; // 5 hours

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> signup(@Valid @RequestBody UserRequest request) {
        User user = userService.createUser(request);
        String token = generateJwtToken(user.getEmail());
        UserResponse userResponse = userMapper.toUserResponse(user);
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        @SuppressWarnings("unused")
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        User user = userService.findByEmail(email);
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        String token = generateJwtToken(user.getEmail());
        UserResponse userResponse = userMapper.toUserResponse(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<Map<String, String>> signout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No token provided"));
        }

        String token = authorizationHeader.substring(7).trim();

        if (blacklistedTokenRepository.existsByToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token already revoked"));
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            LocalDateTime expiryDate = claims.getExpiration().toInstant().atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();

            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(token);
            blacklistedToken.setExpiryDate(expiryDate);
            blacklistedTokenRepository.saveAndFlush(blacklistedToken);

            return ResponseEntity.ok(Map.of("message", "Successfully signed out"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        UserResponse response = userMapper.toUserResponse(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateUserRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setProfileImageUrl(request.getProfileImageUrl());

        user = userService.save(user);
        UserResponse response = userMapper.toUserResponse(user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Map<String, String>> deleteProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);

        user.setActive(false);
        userService.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile deactivated successfully"));
    }

    // Favorites Endpoints
    @GetMapping("/favorites/check/{restaurantId}")
    public ResponseEntity<FavoriteCheckResponse> checkFavorite(@PathVariable Long restaurantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        FavoriteCheckResponse response = new FavoriteCheckResponse();
        response.setFavorited(userService.isFavorite(user.getId(), restaurantId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/favorites/{restaurantId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Map<String, String>> addFavorite(@PathVariable Long restaurantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        userService.addFavorite(user.getId(), restaurantId);
        return ResponseEntity.ok(Map.of("message", "Restaurant added to favorites."));
    }

    @DeleteMapping("/favorites/{restaurantId}")
    public ResponseEntity<Map<String, String>> removeFavorite(@PathVariable Long restaurantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        userService.removeFavorite(user.getId(), restaurantId);
        return ResponseEntity.ok(Map.of("message", "Restaurant removed from favorites."));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<FavoriteResponse>> getFavorites() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        List<Favorites> favorites = userService.getUserFavorites(user.getId());
        List<FavoriteResponse> response = favorites.stream()
                .map(fav -> {
                    FavoriteResponse favResp = new FavoriteResponse();
                    favResp.setRestaurantId(fav.getRestaurant().getId());
                    favResp.setRestaurantName(fav.getRestaurant().getName());
                    favResp.setImageUrl(fav.getRestaurant().getImageUrl());
                    return favResp;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private String generateJwtToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(jwtConfig.getSecretKey())
                .compact();
    }
}
