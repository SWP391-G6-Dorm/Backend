package com.homestay.repository;

import com.homestay.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("SELECT p FROM Payment p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:search IS NULL OR LOWER(p.customer.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(CAST(p.booking.id AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Payment> findAllWithFilters(@Param("status") Payment.Status status,
                                     @Param("search") String search,
                                     Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId ORDER BY p.createdAt DESC")
    java.util.List<Payment> findByBookingIdOrderByCreatedAtDesc(@Param("bookingId") UUID bookingId);

    long countByCustomerIdAndStatus(UUID customerId, Payment.Status status);

    Page<Payment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
}
