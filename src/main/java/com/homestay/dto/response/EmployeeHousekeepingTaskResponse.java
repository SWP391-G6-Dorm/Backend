package com.homestay.dto.response;

import com.homestay.entity.HousekeepingTask;
import com.homestay.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** SCR-60 - Employee housekeeping task list item. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHousekeepingTaskResponse {

    private String id;
    private RoomRef room;
    private String status;
    private String note;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomRef {
        private String id;
        private String roomNumber;
    }

    public static EmployeeHousekeepingTaskResponse fromEntity(HousekeepingTask task) {
        Room room = task.getRoom();
        RoomRef roomRef = null;
        if (room != null) {
            roomRef = RoomRef.builder()
                    .id(room.getId() != null ? room.getId().toString() : null)
                    .roomNumber(room.getRoomNumber())
                    .build();
        }
        return EmployeeHousekeepingTaskResponse.builder()
                .id(task.getId().toString())
                .room(roomRef)
                .status(task.getStatus().name())
                .note(task.getNote())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
