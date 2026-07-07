package com.homestay.repository;

import com.homestay.entity.RoomInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomInspectionRepository extends JpaRepository<RoomInspection, UUID> {

    Optional<RoomInspection> findByBookingId(UUID bookingId);
}
