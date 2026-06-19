package com.homestay.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class FeaturedPropertyResponse {

    private UUID id;
    private String name;
    private String address;
    private int roomCount;
    private int availableRoomCount;
    private String coverImageUrl;
}
