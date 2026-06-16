package com.homestay.dto.request;

import lombok.Data;

@Data
public class UpdatePropertyRequest {
    private String name;
    private String address;
    private String description;
    // ACTIVE hoặc INACTIVE
    private String status;
}
