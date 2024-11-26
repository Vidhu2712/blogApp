package io.mountblue.controller;

import io.mountblue.exception.UserNotFoundException;
import io.mountblue.exception.UserRegistrationException;
import io.mountblue.model.User;
import io.mountblue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "users/user-register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            userService.saveUser(user);
            return "redirect:/login";
        } catch (Exception e) {
            throw new UserRegistrationException("Failed to register user: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable("id") UUID id) {
        if (!userService.existsById(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
        userService.deleteUser(id);
        return "redirect:/users";
    }
}