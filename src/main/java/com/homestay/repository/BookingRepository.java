package com.homestay.repository;

import com.homestay.entity.Booking;
import com.homestay.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByCustomerOrderByCreatedAtDesc(User customer);

    Page<Booking> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Booking> findByCustomerIdAndStatus(UUID customerId, Booking.Status status, Pageable pageable);

    long countByCustomerId(UUID customerId);

    @org.springframework.data.jpa.repository.Query("SELECT b FROM Booking b WHERE " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:search IS NULL OR LOWER(b.customer.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(b.room.roomNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Booking> findAllWithFilters(@org.springframework.data.repository.query.Param("status") Booking.Status status, @org.springframework.data.repository.query.Param("search") String search, Pageable pageable);
}
