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

import java.util.UUID;

@Service
public class UserService {
    @Autowired
    public UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
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
    public boolean isAdmin(){
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email);
        return user!=null && "ADMIN".equals(user.getRole());
    }
    private String getCurrentUserEmail(){
        Object principle = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principle instanceof UserDetails){
            return ((UserDetails) principle).getUsername();
        }else{
            return principle.toString();
        }
    }

    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }
}