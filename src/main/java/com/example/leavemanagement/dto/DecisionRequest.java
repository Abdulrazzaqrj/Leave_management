package com.example.leavemanagement.dto;

// Validation
import jakarta.validation.constraints.Size;

public record DecisionRequest(

        @Size(max = 500)
        String comment

) {
}
