package com.tastopia.tastopia.controller;

import com.tastopia.tastopia.dto.LoginRequest;
import com.tastopia.tastopia.dto.UserRequest;
import com.tastopia.tastopia.dto.UserResponse;
import com.tastopia.tastopia.entity.BlacklistedToken;
import com.tastopia.tastopia.entity.User;
import com.tastopia.tastopia.repository.BlacklistedTokenRepository;
import com.tastopia.tastopia.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000; // 5 hours in milliseconds
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> signup(@Valid @RequestBody UserRequest request) {
        UserResponse userResponse = userService.createUser(request);
        String token = generateJwtToken(userResponse.getEmail());
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));
        User user = userService.findByEmail(email);
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        String token = generateJwtToken(user.getEmail());
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        userResponse.setProfileImageUrl(user.getProfileImageUrl());
        userResponse.setRole(user.getRole());
        userResponse.setIsActive(user.isActive());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<Map<String, String>> signout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        logger.info("Signout request received with Authorization header: {}", authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("No token provided in signout request");
            Map<String, String> response = new HashMap<>();
            response.put("error", "No token provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authorizationHeader.substring(7).trim();
        logger.info("Extracted and trimmed token for signout: {}", token);

        boolean isTokenRevoked = blacklistedTokenRepository.existsByToken(token);
        logger.info("Initial existsByToken check result for {}: {}", token, isTokenRevoked);

        long tokenCount = blacklistedTokenRepository.countByToken(token);
        logger.info("Manual count of token {} in blacklist: {}", token, tokenCount);

        if (isTokenRevoked || tokenCount > 0) {
            logger.warn("Attempted signout with already revoked token: {}", token);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Token already revoked");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 400 Bad Request
        }

        try {
            logger.info("Attempting to parse and blacklist token: {}", token);
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
            LocalDateTime expiryDate = claims.getExpiration().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            String savedToken = token;
            logger.info("Token to be saved to blacklist: {}", savedToken);
            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(savedToken);
            blacklistedToken.setExpiryDate(expiryDate);
            blacklistedTokenRepository.save(blacklistedToken);
            blacklistedTokenRepository.flush(); // Ensure immediate persistence
            logger.info("Token blacklisted successfully: {}", savedToken);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully signed out");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            logger.error("Invalid token during signout: {}, Error: {}", token, e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 Unauthorized for invalid tokens
        }
    }

    private String generateJwtToken(String subject) {
        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
            .signWith(SECRET_KEY)
            .compact();
    }
}