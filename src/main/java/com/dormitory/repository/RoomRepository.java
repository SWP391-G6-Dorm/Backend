package com.dormitory.repository;

import com.dormitory.entity.Room;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    @Query("""
        SELECT r FROM Room r
        JOIN FETCH r.property p
        WHERE r.status = 'AVAILABLE'
        ORDER BY r.id DESC
        """)
    List<Room> findFeaturedRooms(Pageable pageable);

    long countByStatus(Room.Status status);
}
