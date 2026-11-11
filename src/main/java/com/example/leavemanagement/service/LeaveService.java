package com.example.leavemanagement.service;

// DTO Classes
import com.example.leavemanagement.dto.ApplyLeaveRequest;
import com.example.leavemanagement.dto.EmployeeDto;
import com.example.leavemanagement.dto.LeaveDto;

// Entity Classes
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.LeaveStatus;
import com.example.leavemanagement.entity.LeaveType;
import com.example.leavemanagement.entity.Role;

// Repository Classes
import com.example.leavemanagement.repository.EmployeeRepository;
import com.example.leavemanagement.repository.LeaveRequestRepository;

// Spring
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Java Date and Time
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// Java Collections
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaves;
    private final EmployeeRepository employees;

    // Constructor Injection
    public LeaveService(
            LeaveRequestRepository leaves,
            EmployeeRepository employees) {

        this.leaves = leaves;
        this.employees = employees;
    }

    // Find Employee By Email
    private Employee employee(String email) {

        return employees.findByEmail(email)
                .orElseThrow(() ->
                        new NoSuchElementException("Employee not found"));
    }

    // Apply For Leave
    @Transactional
    public LeaveDto apply(
            String email,
            ApplyLeaveRequest request) {

        // Get Employee Details
        Employee employee = employee(email);

        // Get Current Date
        LocalDate today = LocalDate.now();

        // Validate Start Date
        if (request.startDate().isBefore(today)) {

            throw new IllegalArgumentException(
                    "Start date cannot be in the past."
            );
        }

        // Validate End Date
        if (request.endDate().isBefore(request.startDate())) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date."
            );
        }

        // Calculate Total Leave Days
        int days = Math.toIntExact(
                ChronoUnit.DAYS.between(
                        request.startDate(),
                        request.endDate()
                ) + 1
        );

        // Get Available Leave Balance
        int balance = switch (request.type()) {

            case ANNUAL -> employee.getAnnualBalance();

            case SICK -> employee.getSickBalance();

            case OTHER -> employee.getOtherBalance();
        };

        // Check Leave Balance
        if (days > balance) {

            throw new IllegalArgumentException(
                    "Insufficient "
                            + request.type().name().toLowerCase()
                            + " leave balance. Available: "
                            + balance
                            + " days; requested: "
                            + days
                            + " days."
            );
        }

        // Check Overlapping Leave Requests
        boolean overlap =
                leaves.existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(

                        employee.getId(),

                        List.of(
                                LeaveStatus.PENDING,
                                LeaveStatus.APPROVED
                        ),

                        request.endDate(),

                        request.startDate()
                );

        if (overlap) {

            throw new IllegalArgumentException(
                    "This leave overlaps an existing pending or approved request."
            );
        }

        // Create New Leave Request
        LeaveRequest leaveRequest = new LeaveRequest(

                employee,

                request.type(),

                request.startDate(),

                request.endDate(),

                days,

                request.reason()
        );

        // Save Leave Request
        LeaveRequest savedLeave = leaves.save(leaveRequest);

        // Convert Entity Into DTO
        return LeaveDto.from(savedLeave);
    }

    // View Employee Leave History
    public List<LeaveDto> history(String email) {

        Employee employee = employee(email);

        return leaves
                .findByEmployeeIdOrderByAppliedAtDesc(employee.getId())
                .stream()
                .map(LeaveDto::from)
                .toList();
    }

    // View All Leave Requests
    public List<LeaveDto> all() {

        return leaves
                .findAllByOrderByAppliedAtDesc()
                .stream()
                .map(LeaveDto::from)
                .toList();
    }

    // Approve Or Reject Leave
    @Transactional
    public LeaveDto decide(
            Long id,
            boolean approve,
            String comment) {

        // Find Leave Request
        LeaveRequest request = leaves.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Leave request not found"
                        ));

        // Check Pending Status
        if (request.getStatus() != LeaveStatus.PENDING) {

            throw new IllegalArgumentException(
                    "Only pending requests can be approved or rejected."
            );
        }

        // If Manager Approves Leave
        if (approve) {

            Employee employee = request.getEmployee();

            // Deduct Leave Balance Based On Leave Type
            switch (request.getType()) {

                case ANNUAL -> {

                    if (employee.getAnnualBalance() < request.getDays()) {

                        throw new IllegalArgumentException(
                                "Employee no longer has sufficient annual leave balance."
                        );
                    }

                    employee.setAnnualBalance(
                            employee.getAnnualBalance() - request.getDays()
                    );
                }

                case SICK -> {

                    if (employee.getSickBalance() < request.getDays()) {

                        throw new IllegalArgumentException(
                                "Employee no longer has sufficient sick leave balance."
                        );
                    }

                    employee.setSickBalance(
                            employee.getSickBalance() - request.getDays()
                    );
                }

                case OTHER -> {

                    if (employee.getOtherBalance() < request.getDays()) {

                        throw new IllegalArgumentException(
                                "Employee no longer has sufficient other leave balance."
                        );
                    }

                    employee.setOtherBalance(
                            employee.getOtherBalance() - request.getDays()
                    );
                }
            }

            // Save Updated Employee Balance
            employees.save(employee);

            // Update Leave Status To Approved
            request.decide(
                    LeaveStatus.APPROVED,
                    comment
            );

        } else {

            // Update Leave Status To Rejected
            request.decide(
                    LeaveStatus.REJECTED,
                    comment
            );
        }

        // Save Leave Request
        LeaveRequest updatedRequest = leaves.save(request);

        // Return Updated Leave Details
        return LeaveDto.from(updatedRequest);
    }

    // Get Employee Profile
    public EmployeeDto profile(String email) {

        Employee employee = employee(email);

        return EmployeeDto.from(employee);
    }

    // Get All Employees
    public List<EmployeeDto> employees() {

        return employees.findAll()
                .stream()

                // Only Employees, Excluding Managers
                .filter(employee ->
                        employee.getRole() == Role.EMPLOYEE
                )

                .map(EmployeeDto::from)
                .toList();
    }
}
