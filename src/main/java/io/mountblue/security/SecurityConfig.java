package io.mountblue.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorize -> authorize
                        .requestMatchers( "/blog/**", "/posts/**","/posts/delete/{id}").hasAnyRole("USER","ADMIN")
                        .requestMatchers("/create", "/edit/**").hasAnyRole("USER","ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(
                        form-> form
                                .defaultSuccessUrl("/",true)
                                .permitAll()

                )
                .logout(logout->logout
                        .logoutSuccessUrl("/")
                        .permitAll()

                );

        return http.build();
    }
}
