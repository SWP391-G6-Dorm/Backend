package com.homestay.service;

import com.homestay.dto.response.EmployeeRoomResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.entity.EmployeePropertyAssignment;
import com.homestay.entity.Room;
import com.homestay.entity.User;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.EmployeePropertyAssignmentRepository;
import com.homestay.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for SCR-65 — Employee read-only room list.
 * Scoped to the property the authenticated employee is actively assigned to.
 */
@Service
public class EmployeeRoomService {

    private final EmployeePropertyAssignmentRepository assignmentRepository;
    private final RoomRepository roomRepository;

    public EmployeeRoomService(EmployeePropertyAssignmentRepository assignmentRepository,
                               RoomRepository roomRepository) {
        this.assignmentRepository = assignmentRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * Returns paginated rooms for the property the given employee is assigned to.
     *
     * @param currentUser the authenticated employee
     * @param status      optional Room.Status filter (null = all statuses)
     * @param page        zero-based page number
     * @param size        page size
     */
    @Transactional(readOnly = true)
    public PageResponse<EmployeeRoomResponse> getRoomsForEmployee(User currentUser,
                                                                   String status,
                                                                   int page,
                                                                   int size) {
        // 1. Resolve employee's active property assignment
        EmployeePropertyAssignment assignment = assignmentRepository
                .findByEmployeeIdAndStatus(currentUser.getId(), EmployeePropertyAssignment.Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee không được gán vào property nào. Liên hệ Manager để được phân công."));

        UUID propertyId = assignment.getProperty().getId();

        // 2. Build pageable — order by roomNumber ascending for easy reference
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "roomNumber"));

        // 3. Query rooms, optionally filtered by status
        Page<Room> roomPage;
        if (status != null && !status.isBlank()) {
            Room.Status roomStatus = Room.Status.valueOf(status.trim().toUpperCase());
            roomPage = roomRepository.findByPropertyIdAndStatus(propertyId, roomStatus, pageable);
        } else {
            roomPage = roomRepository.findByPropertyId(propertyId, pageable);
        }

        // 4. Map entity → DTO
        Page<EmployeeRoomResponse> dtoPage = roomPage.map(this::toResponse);

        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages()
        );
    }

    // ── Private mapper ────────────────────────────────────────────────────────

    private EmployeeRoomResponse toResponse(Room room) {
        return new EmployeeRoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType(),
                room.getStatus() != null ? room.getStatus().name() : null,
                room.getCapacity(),
                room.getPricePerNight(),
                room.getFloor() != null ? "Tầng " + room.getFloor().getFloorNumber() : null
        );
    }
}
