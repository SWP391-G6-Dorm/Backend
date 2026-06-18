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
    // Booking trùng khi: checkIn mới < checkOut cũ VÀ checkOut mới > checkIn cũ
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
}
