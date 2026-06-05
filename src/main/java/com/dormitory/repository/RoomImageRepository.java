package com.dormitory.repository;

import com.dormitory.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, UUID> {
    Optional<RoomImage> findFirstByRoomIdOrderBySortOrderAsc(UUID roomId);
}
