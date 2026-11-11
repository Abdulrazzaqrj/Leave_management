package com.example.leavemanagement.repository;

// Employee Entity
import com.example.leavemanagement.entity.Employee;

// Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository;

// Java Utilities
import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    // Find Employee Using Email
    Optional<Employee> findByEmail(String email);

}
