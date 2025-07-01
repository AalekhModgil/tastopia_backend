package com.tastopia.tastopia.service;

import com.tastopia.tastopia.dto.UserRequest;
import com.tastopia.tastopia.entity.Favorites;
import com.tastopia.tastopia.entity.Restaurant;
import com.tastopia.tastopia.entity.User;
import com.tastopia.tastopia.exception.DuplicateFavoriteException;
import com.tastopia.tastopia.exception.DuplicateUserDetailsException;
import com.tastopia.tastopia.exception.ResourceNotFoundException;
import com.tastopia.tastopia.repository.FavoritesRepository;
import com.tastopia.tastopia.repository.RestaurantRepository;
import com.tastopia.tastopia.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final FavoritesRepository favoritesRepository;
    private final RestaurantRepository restaurantRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public User createUser(UserRequest request) {
        Map<String, String> errors = new HashMap<>();

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> errors.put("email", "Email already taken"));

        userRepository.findByPhone(request.getPhone())
                .ifPresent(user -> errors.put("phone", "Phone number already taken"));

        if (!errors.isEmpty()) {
            throw new DuplicateUserDetailsException("Validation errors", errors);
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword())); // Hash with BCrypt
        user.setPhone(request.getPhone());
        user.setProfileImageUrl(request.getProfileImageUrl());
        user.setRole(User.Role.USER);
        user.setActive(true);

        return userRepository.save(user);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public boolean isFavorite(Long userId, Long restaurantId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId);
        }
        return favoritesRepository.existsByUserIdAndRestaurantId(userId, restaurantId);
    }

    public Favorites addFavorite(Long userId, Long restaurantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));
        if (favoritesRepository.existsByUserIdAndRestaurantId(userId, restaurantId)) {
            throw new DuplicateFavoriteException("Restaurant already in favorites");
        }
        Favorites favorite = new Favorites();
        favorite.setUser(user);
        favorite.setRestaurant(restaurant);
        return favoritesRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long userId, Long restaurantId) {
        if (!favoritesRepository.existsByUserIdAndRestaurantId(userId, restaurantId)) {
            throw new ResourceNotFoundException("Favorite not found");
        }
        favoritesRepository.deleteByUserIdAndRestaurantId(userId, restaurantId);
    }

    public List<Favorites> getUserFavorites(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return favoritesRepository.findByUserId(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}