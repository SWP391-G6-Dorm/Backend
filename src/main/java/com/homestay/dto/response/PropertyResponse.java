package com.homestay.dto.response;

import com.homestay.entity.Property;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

// Response tóm tắt dùng trong danh sách
@Data
public class PropertyResponse {

    private UUID id;
    private String name;
    private String address;
    private String description;
    private String status;
    private int totalFloors;
    private int totalRooms;
    private LocalDateTime createdAt;

    public static PropertyResponse fromEntity(Property property) {
        PropertyResponse res = new PropertyResponse();
        res.setId(property.getId());
        res.setName(property.getName());
        res.setAddress(property.getAddress());
        res.setDescription(property.getDescription());
        res.setStatus(property.getStatus().name());
        res.setTotalFloors(property.getFloors() != null ? property.getFloors().size() : 0);
        res.setTotalRooms(property.getRooms() != null ? property.getRooms().size() : 0);
        res.setCreatedAt(property.getCreatedAt());
        return res;
    }
}
