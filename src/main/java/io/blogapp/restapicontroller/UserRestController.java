package io.blogapp.restapicontroller;

import io.blogapp.exception.UserNotFoundException;
import io.blogapp.exception.UserRegistrationException;
import io.blogapp.model.User;
import io.blogapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import io.blogapp.exception.UserNotFoundException;
import io.blogapp.exception.UserRegistrationException;
import io.blogapp.model.User;
import io.blogapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            userService.saveUser(user);
            return ResponseEntity.status(201).body("User registered successfully.");
        } catch (Exception e) {
            throw new UserRegistrationException("Failed to register user: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") UUID id) {
        if (!userService.existsById(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }
}

