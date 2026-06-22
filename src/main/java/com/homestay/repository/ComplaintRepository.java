package com.homestay.repository;

import com.homestay.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:search IS NULL OR LOWER(c.subject) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR (c.customer IS NOT NULL AND LOWER(c.customer.fullName) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<Complaint> findByFilters(
            @Param("status") Complaint.Status status,
            @Param("search") String search,
            Pageable pageable
    );
}
