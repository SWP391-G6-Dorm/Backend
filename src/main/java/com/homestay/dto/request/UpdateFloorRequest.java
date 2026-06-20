package com.homestay.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateFloorRequest {

    @Min(value = 1, message = "Số tầng phải >= 1")
    private Integer floorNumber;

    private String description;
}
