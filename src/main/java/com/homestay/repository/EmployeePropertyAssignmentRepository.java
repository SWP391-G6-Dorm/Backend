package com.homestay.repository;

import com.homestay.entity.EmployeePropertyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeePropertyAssignmentRepository extends JpaRepository<EmployeePropertyAssignment, UUID> {

    // Lấy assignment active của employee (một employee chỉ thuộc 1 property tại 1 thời điểm)
    Optional<EmployeePropertyAssignment> findByEmployeeIdAndStatus(
            UUID employeeId,
            EmployeePropertyAssignment.Status status
    );
}
