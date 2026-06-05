package com.dormitory.mapper;

import com.dormitory.dto.response.FeaturedRoomDto;
import com.dormitory.entity.Room;
import com.dormitory.entity.RoomImage;
import com.dormitory.repository.RoomImageRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomMapper {

    private final ObjectMapper objectMapper;
    private final RoomImageRepository roomImageRepository;

    public FeaturedRoomDto toFeaturedRoomDto(Room room) {
        if (room == null) {
            return null;
        }

        String imageUrl = roomImageRepository
                .findFirstByRoomIdOrderBySortOrderAsc(room.getId())
                .map(RoomImage::getImageUrl)
                .orElse(null);

        List<String> amenities = parseAmenities(room.getAmenities());

        return new FeaturedRoomDto(
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType(),
                room.getPricePerMonth(),
                room.getCapacity(),
                room.getGenderType() != null ? room.getGenderType().name() : null,
                room.getStatus() != null ? room.getStatus().name() : null,
                room.getProperty() != null ? room.getProperty().getName() : null,
                room.getProperty() != null ? room.getProperty().getAddress() : null,
                imageUrl,
                amenities
        );
    }

    private List<String> parseAmenities(String amenitiesJson) {
        if (amenitiesJson == null || amenitiesJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(amenitiesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
