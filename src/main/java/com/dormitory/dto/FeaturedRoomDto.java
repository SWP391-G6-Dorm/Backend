package com.dormitory.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
