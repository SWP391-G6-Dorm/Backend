package com.dormitory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for public room listing on Landing Page.
 * Used by GET /api/public/rooms/featured
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedRoomDto {

    private UUID id;
    private String roomNumber;
    private String roomType;
    private BigDecimal pricePerMonth;
    private Integer capacity;
    private String genderType;
    private String status;
    private String propertyName;
    private String address;
    private String imageUrl;
    private List<String> amenities;
}
