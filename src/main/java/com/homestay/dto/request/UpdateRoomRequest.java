package com.homestay.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateRoomRequest {
    private String roomNumber;
    private String roomType;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private BigDecimal area;
    private String description;
}
