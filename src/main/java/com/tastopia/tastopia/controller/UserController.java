package com.tastopia.tastopia.controller;

import com.tastopia.tastopia.dto.LoginRequest;
import com.tastopia.tastopia.dto.UserRequest;
import com.tastopia.tastopia.dto.UserResponse;
import com.tastopia.tastopia.entity.BlacklistedToken;
import com.tastopia.tastopia.entity.User;
import com.tastopia.tastopia.mapper.UserMapper;
import com.tastopia.tastopia.repository.BlacklistedTokenRepository;
import com.tastopia.tastopia.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private final UserMapper userMapper;
    private static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000; // 5 hours in milliseconds
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

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
    public ResponseEntity<Map<String, String>> signout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "No token provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authorizationHeader.substring(7).trim();

        boolean isTokenRevoked = blacklistedTokenRepository.existsByToken(token);
        long tokenCount = blacklistedTokenRepository.countByToken(token);

        if (isTokenRevoked || tokenCount > 0) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Token already revoked");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
            LocalDateTime expiryDate = claims.getExpiration().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            String savedToken = token;
            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(savedToken);
            blacklistedToken.setExpiryDate(expiryDate);
            blacklistedTokenRepository.save(blacklistedToken);
            blacklistedTokenRepository.flush();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully signed out");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
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