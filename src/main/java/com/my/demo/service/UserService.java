package com.my.demo.service;

import com.my.demo.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    User updateUser(Long id, User user);
    Optional<User> getUserByEmail(String email);
    void deleteUser(Long id);
}
