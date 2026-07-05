package com.homestay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO trả về cho SCR-65 — Employee view of a room (read-only reference).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRoomResponse {

    private UUID id;
    private String roomNumber;
    private String roomType;
    private String status;       // Room.Status enum value as string
    private Integer capacity;
    private BigDecimal pricePerNight;
    private String floorName;    // Tên tầng để hiện trên card
}
