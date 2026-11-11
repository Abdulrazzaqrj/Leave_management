package com.example.leavemanagement.dto;

// Entity Classes
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.entity.LeaveRequest;

// Date and Time
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveDto(

        Long id,

        Long employeeId,

        String employeeName,

        String employeeEmail,

        String type,

        String status,

        LocalDate startDate,

        LocalDate endDate,

        int days,

        String reason,

        String managerComment,

        LocalDateTime appliedAt,

        LocalDateTime decidedAt

) {

    // Convert LeaveRequest Entity into LeaveDto
    public static LeaveDto from(LeaveRequest request) {

        Employee employee = request.getEmployee();

        return new LeaveDto(

                request.getId(),

                employee.getId(),

                employee.getName(),

                employee.getEmail(),

                request.getType().name(),

                request.getStatus().name(),

                request.getStartDate(),

                request.getEndDate(),

                request.getDays(),

                request.getReason(),

                request.getManagerComment(),

                request.getAppliedAt(),

                request.getDecidedAt()
        );
    }
}
