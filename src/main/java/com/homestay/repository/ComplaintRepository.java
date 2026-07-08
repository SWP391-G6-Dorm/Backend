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
           "OR (c.user IS NOT NULL AND LOWER(c.user.fullName) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<Complaint> findByFilters(
            @Param("status") Complaint.Status status,
            @Param("search") String search,
            Pageable pageable
    );

    // SCR-54: Admin complaint list. LEFT JOIN FETCH user (nullable guest). Enum via param.
    @Query(value = """
            SELECT c FROM Complaint c
            LEFT JOIN FETCH c.user u
            WHERE (:status IS NULL OR c.status = :status)
            ORDER BY c.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(c) FROM Complaint c
            WHERE (:status IS NULL OR c.status = :status)
            """)
    Page<Complaint> findForAdmin(
            @Param("status") Complaint.Status status,
            Pageable pageable);
}
