package com.example.leavemanagement.dto;

// Employee Entity
import com.example.leavemanagement.entity.Employee;

public record EmployeeDto(

        Long id,

        String name,

        String email,

        String role,

        int annualBalance,

        int sickBalance,

        int otherBalance

) {

    // Convert Employee Entity into EmployeeDto
    public static EmployeeDto from(Employee employee) {

        return new EmployeeDto(

                employee.getId(),

                employee.getName(),

                employee.getEmail(),

                employee.getRole().name(),

                employee.getAnnualBalance(),

                employee.getSickBalance(),

                employee.getOtherBalance()
        );
    }
}
