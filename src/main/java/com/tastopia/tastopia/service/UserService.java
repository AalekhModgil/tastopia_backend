package com.tastopia.tastopia.service;

import com.tastopia.tastopia.dto.UserRequest;
import com.tastopia.tastopia.dto.UserResponse;
import com.tastopia.tastopia.entity.User;
import com.tastopia.tastopia.exception.DuplicateEmailException;
import com.tastopia.tastopia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(UserRequest request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new DuplicateEmailException("Email already taken");
                });

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // In production, hash password

        User savedUser = userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        return response;
    }
}