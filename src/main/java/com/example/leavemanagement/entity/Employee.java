package com.example.leavemanagement.entity;

// JPA Imports
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Version;

@Entity
@Table(name = "employees")
public class Employee {

    // Employee ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Employee Name
    @Column(nullable = false)
    private String name;

    // Employee Email
    @Column(nullable = false, unique = true)
    private String email;

    // Employee Password
    @Column(nullable = false)
    private String password;

    // Employee Role
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.EMPLOYEE;

    // Leave Balances
    private int annualBalance = 12;

    private int sickBalance = 10;

    private int otherBalance = 3;

    // Optimistic Locking Version
    @Version
    private Long version;

    // Default Constructor
    public Employee() {
    }

    // Parameterized Constructor
    public Employee(
            String name,
            String email,
            String password,
            Role role,
            int annual,
            int sick,
            int other) {

        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.annualBalance = annual;
        this.sickBalance = sick;
        this.otherBalance = other;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public int getAnnualBalance() {
        return annualBalance;
    }

    public int getSickBalance() {
        return sickBalance;
    }

    public int getOtherBalance() {
        return otherBalance;
    }

    // Setters

    public void setAnnualBalance(int value) {
        this.annualBalance = value;
    }

    public void setSickBalance(int value) {
        this.sickBalance = value;
    }

    public void setOtherBalance(int value) {
        this.otherBalance = value;
    }
}
