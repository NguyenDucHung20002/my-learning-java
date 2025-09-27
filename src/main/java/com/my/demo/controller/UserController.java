package com.my.demo.controller;

import com.my.demo.annotation.ThrowsMessage;
import com.my.demo.dto.UserDTO;
import com.my.demo.exception.errors.EmailAlreadyExistsException;
import com.my.demo.model.User;
import com.my.demo.response.ApiMessage;
import com.my.demo.response.ApiResponse;
import com.my.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @ThrowsMessage(value = "User created successfully", code = 201)
    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody User user) {
        var checkEmail = userService.getUserByEmail(user.getEmail());
        if (checkEmail.isPresent()) {
            throw new EmailAlreadyExistsException("Email is already in use");
        }

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userService.saveUser(user);
        UserDTO userDTO = new UserDTO(savedUser.getUsername(), savedUser.getEmail());

        ApiResponse<UserDTO> response = ApiResponse.created(userDTO, "User created successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @ThrowsMessage(value = "User retrieved successfully", code = 200)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> {
                    UserDTO userDTO = new UserDTO(user.getUsername(), user.getEmail());
                    ApiResponse<UserDTO> response = ApiResponse.success(userDTO, "User retrieved successfully");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    ApiResponse<UserDTO> response = ApiResponse.error(404, "User not found", "USER_NOT_FOUND");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    @ThrowsMessage(value = "Current user profile retrieved successfully", code = 200)
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            ApiResponse<UserDTO> response = ApiResponse.error(401, "User not authenticated", "NOT_AUTHENTICATED");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByUsername(username);

        if (userOptional.isEmpty()) {
            // Try to find by email if username search fails
            userOptional = userService.getUserByEmail(username);
        }

        return userOptional
                .map(user -> {
                    UserDTO userDTO = new UserDTO(user.getUsername(), user.getEmail());
                    ApiResponse<UserDTO> response = ApiResponse.success(userDTO, "Current user profile retrieved successfully");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    ApiResponse<UserDTO> response = ApiResponse.error(404, "User profile not found", "USER_PROFILE_NOT_FOUND");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }
}
