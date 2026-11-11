package com.example.leavemanagement.repository;

// Entity Classes
import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.LeaveStatus;

// Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository;

// Date
import java.time.LocalDate;

// Collections
import java.util.List;
import java.util.Collection;

public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long> {

    // Get Employee Leave History
    List<LeaveRequest> findByEmployeeIdOrderByAppliedAtDesc(
            Long employeeId
    );

    // Get All Leave Requests
    List<LeaveRequest> findAllByOrderByAppliedAtDesc();

    // Check Overlapping Leave Requests
    boolean existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long id,
            Collection<LeaveStatus> statuses,
            LocalDate end,
            LocalDate start
    );

}
