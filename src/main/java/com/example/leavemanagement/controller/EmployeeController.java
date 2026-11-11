package com.example.leavemanagement.controller;

// DTO Classes
import com.example.leavemanagement.dto.ApplyLeaveRequest;
import com.example.leavemanagement.dto.EmployeeDto;
import com.example.leavemanagement.dto.LeaveDto;

// Service
import com.example.leavemanagement.service.LeaveService;

// Validation
import jakarta.validation.Valid;

// Spring Security
import org.springframework.security.core.Authentication;

// Spring Web
import org.springframework.web.bind.annotation.*;

// Java Utilities
import java.util.List;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private final LeaveService service;

    // Constructor Injection
    public EmployeeController(LeaveService service) {
        this.service = service;
    }

    // Get Employee Profile
    @GetMapping("/profile")
    public EmployeeDto profile(Authentication authentication) {

        return service.profile(authentication.getName());
    }

    // Apply for Leave
    @PostMapping("/leaves")
    public LeaveDto apply(
            Authentication authentication,
            @Valid @RequestBody ApplyLeaveRequest request) {

        return service.apply(
                authentication.getName(),
                request
        );
    }

    // View Leave History
    @GetMapping("/leaves")
    public List<LeaveDto> history(Authentication authentication) {

        return service.history(authentication.getName());
    }
}
