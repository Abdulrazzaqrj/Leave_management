
package com.example.leavemanagement.config;

// Repository
import com.example.leavemanagement.repository.EmployeeRepository;

// Spring Configuration
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Spring Security Authentication
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

// HTTP Security
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// Password Encryption
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Security Filter
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // User Details Service
    @Bean
    public UserDetailsService userDetailsService(EmployeeRepository repo) {

        return email -> repo.findByEmail(email)
                .map(employee -> User.withUsername(employee.getEmail())
                        .password(employee.getPassword())
                        .roles(employee.getRole().name())
                        .build())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }

    // Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    // Security Filter Chain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http

                // Disable CSRF
                .csrf(csrf -> csrf.disable())

                // Request Authorization
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/app.js",
                                "/styles.css",
                                "/api/auth/login",
                                "/api/auth/logout"
                        ).permitAll()

                        // Manager Access
                        .requestMatchers("/api/manager/**")
                        .hasRole("MANAGER")

                        // Employee Access
                        .requestMatchers("/api/employee/**")
                        .hasRole("EMPLOYEE")

                        // Other Requests
                        .anyRequest().authenticated()
                )

                // Disable Default Login
                .formLogin(form -> form.disable())

                // Disable HTTP Basic
                .httpBasic(basic -> basic.disable())

                // Custom Logout Handling
                .logout(logout -> logout.disable())

                // Security Context Configuration
                .securityContext(context ->
                        context.requireExplicitSave(false)
                )

                .build();
    }
}
