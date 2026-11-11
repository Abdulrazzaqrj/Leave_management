package com.example.leavemanagement.controller;

// DTO Classes
import com.example.leavemanagement.dto.DecisionRequest;
import com.example.leavemanagement.dto.EmployeeDto;
import com.example.leavemanagement.dto.LeaveDto;

// Service
import com.example.leavemanagement.service.LeaveService;

// Validation
import jakarta.validation.Valid;

// Spring Web
import org.springframework.web.bind.annotation.*;

// Java Utilities
import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final LeaveService service;

    // Constructor Injection
    public ManagerController(LeaveService service) {
        this.service = service;
    }

    // Get All Leave Requests
    @GetMapping("/leaves")
    public List<LeaveDto> all() {

        return service.all();
    }

    // Get All Employees
    @GetMapping("/employees")
    public List<EmployeeDto> employees() {

        return service.employees();
    }

    // Approve Leave Request
    @PutMapping("/leaves/{id}/approve")
    public LeaveDto approve(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) DecisionRequest request) {

        return service.decide(
                id,
                true,
                request == null ? null : request.comment()
        );
    }

    // Reject Leave Request
    @PutMapping("/leaves/{id}/reject")
    public LeaveDto reject(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) DecisionRequest request) {

        return service.decide(
                id,
                false,
                request == null ? null : request.comment()
        );
    }
}
