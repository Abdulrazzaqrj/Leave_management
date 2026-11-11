package com.example.leavemanagement.entity;

// JPA Imports
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;

// Date and Time
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    // Leave Request ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Employee Relationship
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // Leave Type
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;

    // Leave Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status = LeaveStatus.PENDING;

    // Leave Dates
    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // Total Leave Days
    @Column(nullable = false)
    private int days;

    // Leave Reason
    @Column(length = 1000)
    private String reason;

    // Manager Comment
    private String managerComment;

    // Application Date
    private LocalDateTime appliedAt = LocalDateTime.now();

    // Decision Date
    private LocalDateTime decidedAt;

    // Default Constructor
    public LeaveRequest() {
    }

    // Parameterized Constructor
    public LeaveRequest(
            Employee employee,
            LeaveType type,
            LocalDate startDate,
            LocalDate endDate,
            int days,
            String reason) {

        this.employee = employee;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.days = days;
        this.reason = reason;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public LeaveType getType() {
        return type;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getDays() {
        return days;
    }

    public String getReason() {
        return reason;
    }

    public String getManagerComment() {
        return managerComment;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    // Approve or Reject Leave
    public void decide(LeaveStatus status, String comment) {

        this.status = status;
        this.managerComment = comment;
        this.decidedAt = LocalDateTime.now();
    }
}
