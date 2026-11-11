package com.example.leavemanagement.controller;

// Service
import com.example.leavemanagement.service.LeaveService;

// Servlet
import jakarta.servlet.http.HttpServletRequest;

// Spring HTTP
import org.springframework.http.ResponseEntity;

// Spring Security
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

// Spring Web
import org.springframework.web.bind.annotation.*;

// Java Utilities
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager auth;
    private final LeaveService service;

    // Constructor Injection
    public AuthController(AuthenticationManager auth, LeaveService service) {
        this.auth = auth;
        this.service = service;
    }

    // Login Request DTO
    public record LoginRequest(String email, String password) {
    }

    // Employee / Manager Login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req,
            HttpServletRequest request) {

        try {

            // Authenticate user
            Authentication authentication = auth.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.email(),
                            req.password()
                    )
            );

            // Create Security Context
            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

            // Store Security Context in HTTP Session
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository
                            .SPRING_SECURITY_CONTEXT_KEY,
                    context
            );

            // Return User Profile
            return ResponseEntity.ok(
                    Map.of(
                            "user",
                            service.profile(req.email())
                    )
            );

        } catch (AuthenticationException ex) {

            // Invalid Login
            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "error",
                                    "Invalid email or password."
                            )
                    );
        }
    }

    // Get Logged-in User
    @GetMapping("/me")
    public Object me(Authentication authentication) {

        return service.profile(authentication.getName());
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Logged out successfully."
                )
        );
    }
}
