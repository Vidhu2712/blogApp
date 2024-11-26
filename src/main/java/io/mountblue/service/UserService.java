package io.mountblue.service;

import io.mountblue.dao.UserRepository;
import io.mountblue.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    public UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void deleteUser(UUID id) {
         userRepository.deleteById(id);
    }

    public User findByUsername(String authorName) {
        return userRepository.findByName(authorName);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)){
            String username = authentication.getName();
            return getUserByEmail(username);
        }
        return null;
    }
    public boolean isAdmin() {
        // Get the current logged-in user's email
        String email = getCurrentUserEmail();

        // Fetch the user from the database
        User user = userRepository.findByEmail(email);

        // Check if the user exists and has the 'ADMIN' role
        return user != null && "ADMIN".equals(user.getRole());
    }

    public String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}
