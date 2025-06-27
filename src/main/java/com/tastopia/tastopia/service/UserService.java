package com.tastopia.tastopia.service;

import com.tastopia.tastopia.dto.UserRequest;
import com.tastopia.tastopia.entity.User;
import com.tastopia.tastopia.exception.DuplicateEmailException;
import com.tastopia.tastopia.exception.DuplicatePhoneException;
import com.tastopia.tastopia.exception.ResourceNotFoundException;
import com.tastopia.tastopia.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public User createUser(UserRequest request) {
        userRepository.findByEmail(request.getEmail())
            .ifPresent(user -> { throw new DuplicateEmailException("Email already taken"); });

        userRepository.findByPhone(request.getPhone())
            .ifPresent(user -> { throw new DuplicatePhoneException("Phone number already taken"); });

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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(), user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}