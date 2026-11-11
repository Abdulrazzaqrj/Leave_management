package com.example.leavemanagement.config;

import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.repository.EmployeeRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seed(
            EmployeeRepository repo,
            PasswordEncoder encoder) {

        return args -> {

            // Check whether employees already exist
            if (repo.count() == 0) {

                // Create Manager
                Employee manager = new Employee(
                        "Manager Admin",
                        "manager@company.com",
                        encoder.encode("Manager@123"),
                        Role.MANAGER,
                        0,
                        0,
                        0
                );

                repo.save(manager);

                // Create First Employee
                Employee employee1 = new Employee(
                        "Davis Levin",
                        "davis@company.com",
                        encoder.encode("Employee@123"),
                        Role.EMPLOYEE,
                        12,
                        10,
                        3
                );

                repo.save(employee1);

                // Create Second Employee
                Employee employee2 = new Employee(
                        "Lina Ahmad",
                        "lina@company.com",
                        encoder.encode("Employee@123"),
                        Role.EMPLOYEE,
                        12,
                        10,
                        3
                );

                repo.save(employee2);

            }
        };
    }
}
