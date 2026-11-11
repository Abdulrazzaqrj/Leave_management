package com.example.leavemanagement.dto;

// Leave Type Enum
import com.example.leavemanagement.entity.LeaveType;

// Validation
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Date
import java.time.LocalDate;

public record ApplyLeaveRequest(

        @NotNull
        LeaveType type,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotBlank
        @Size(max = 1000)
        String reason

) {
}
