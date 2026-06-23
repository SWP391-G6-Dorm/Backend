package com.homestay.repository;

import com.homestay.entity.Booking;
import com.homestay.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByCustomerOrderByCreatedAtDesc(User customer);

    Page<Booking> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Booking> findByCustomerIdAndStatus(UUID customerId, Booking.Status status, Pageable pageable);

    Page<Booking> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    long countByCustomerId(UUID customerId);

    @org.springframework.data.jpa.repository.Query("SELECT b FROM Booking b WHERE " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:search IS NULL OR LOWER(b.customer.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(b.room.roomNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Booking> findAllWithFilters(@Param("status") Booking.Status status, @Param("search") String search, Pageable pageable);

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.customer.id = :customerId
        AND b.status IN ('PENDING_DEPOSIT', 'CONFIRMED', 'CHECKED_IN')
        """)
    long countActiveByCustomerId(@Param("customerId") UUID customerId);

    java.util.Optional<Booking> findFirstByCustomerIdAndStatusAndCheckInDateGreaterThanEqualOrderByCheckInDateAsc(
            UUID customerId, Booking.Status status, LocalDate checkInDate);

    java.util.Optional<Booking> findFirstByCustomerIdAndStatusAndCheckOutDateGreaterThanEqualOrderByCheckOutDateAsc(
            UUID customerId, Booking.Status status, LocalDate checkOutDate);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.customer.id = :customerId
        AND b.status IN ('PENDING_DEPOSIT', 'CONFIRMED', 'CHECKED_IN')
        AND (b.status = 'CHECKED_IN' OR b.checkInDate >= :today)
        ORDER BY b.checkInDate ASC
        """)
    List<Booking> findUpcomingByCustomerId(@Param("customerId") UUID customerId, @Param("today") LocalDate today);
}
