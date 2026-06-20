package com.homestay.repository;

import com.homestay.entity.Room;
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
public interface RoomRepository extends JpaRepository<Room, UUID> {

    // Tìm phòng theo property
    Page<Room> findByPropertyId(UUID propertyId, Pageable pageable);

    // Tìm phòng theo floor
    List<Room> findByFloorId(UUID floorId);

    // Lọc phòng theo status
    Page<Room> findByStatus(Room.Status status, Pageable pageable);

    // Đếm phòng theo status — dùng cho Dashboard KPI
    long countByStatus(Room.Status status);

    // Tìm phòng available theo property
    Page<Room> findByPropertyIdAndStatus(UUID propertyId, Room.Status status, Pageable pageable);

    // Tìm kiếm phòng theo tên phòng, loại phòng
    Page<Room> findByRoomNumberContainingIgnoreCaseOrRoomTypeContainingIgnoreCase(
            String roomNumber, String roomType, Pageable pageable);

    // Kiểm tra phòng có bị trùng lịch không
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.room.id = :roomId
        AND b.status NOT IN ('CANCELLED')
        AND b.checkInDate < :checkOut
        AND b.checkOutDate > :checkIn
    """)
    boolean existsOverlapBooking(@Param("roomId") UUID roomId,
                                 @Param("checkIn") LocalDate checkIn,
                                 @Param("checkOut") LocalDate checkOut);

    // Lấy các ngày đã bị đặt của phòng (dùng cho calendar)
    @Query("""
        SELECT b.checkInDate, b.checkOutDate FROM Booking b
        WHERE b.room.id = :roomId
        AND b.status NOT IN ('CANCELLED')
        AND b.checkOutDate >= CURRENT_DATE
        ORDER BY b.checkInDate
    """)
    List<Object[]> findBookedDateRanges(@Param("roomId") UUID roomId);

    // SCR-39: Combined filter dành cho Manager — hỗ trợ search + status + propertyId + floorId + roomType
    @Query("""
        SELECT r FROM Room r
        WHERE (:search IS NULL OR
               LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(r.roomType)   LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:status    IS NULL OR r.status        = :status)
        AND (:propertyId IS NULL OR r.property.id = :propertyId)
        AND (:floorId    IS NULL OR r.floor.id    = :floorId)
        AND (:roomType   IS NULL OR LOWER(r.roomType) = LOWER(:roomType))
    """)
    Page<Room> findWithFilters(
            @Param("search")     String search,
            @Param("status")     Room.Status status,
            @Param("propertyId") UUID propertyId,
            @Param("floorId")    UUID floorId,
            @Param("roomType")   String roomType,
            Pageable pageable
    );

    // SCR-39: Kiểm tra phòng có booking active không (trước khi xóa)
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.room.id = :roomId
        AND b.status NOT IN ('CANCELLED', 'COMPLETED')
    """)
    boolean hasActiveBookings(@Param("roomId") UUID roomId);
}

