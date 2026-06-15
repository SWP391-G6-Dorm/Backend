package com.homestay.service;

import com.homestay.dto.request.CreateFloorRequest;
import com.homestay.dto.response.FloorResponse;
import com.homestay.entity.Floor;
import com.homestay.entity.Property;
import com.homestay.exception.BusinessException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.FloorRepository;
import com.homestay.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FloorService {

    private final FloorRepository floorRepository;
    private final PropertyRepository propertyRepository;

    public FloorService(FloorRepository floorRepository, PropertyRepository propertyRepository) {
        this.floorRepository = floorRepository;
        this.propertyRepository = propertyRepository;
    }

    // Lấy danh sách tầng của property
    public List<FloorResponse> getByProperty(UUID propertyId) {
        Property property = findPropertyById(propertyId);
        return floorRepository.findByPropertyOrderByFloorNumberAsc(property)
                .stream().map(FloorResponse::fromEntity).collect(Collectors.toList());
    }

    // Tạo tầng mới
    @Transactional
    public FloorResponse create(CreateFloorRequest request) {
        UUID propertyId = UUID.fromString(request.getPropertyId());
        Property property = findPropertyById(propertyId);

        // Kiểm tra số tầng đã tồn tại chưa
        if (floorRepository.existsByPropertyAndFloorNumber(property, request.getFloorNumber())) {
            throw new BusinessException("Tầng " + request.getFloorNumber() + " đã tồn tại trong property này");
        }

        Floor floor = new Floor();
        floor.setProperty(property);
        floor.setFloorNumber(request.getFloorNumber());
        floor.setDescription(request.getDescription());
        floorRepository.save(floor);

        return FloorResponse.fromEntity(floor);
    }

    // Xóa tầng - không cho xóa nếu còn phòng
    @Transactional
    public void delete(UUID floorId) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tầng"));

        if (floor.getRooms() != null && !floor.getRooms().isEmpty()) {
            throw new BusinessException("Không thể xóa tầng đang có phòng. Vui lòng xóa phòng trước.");
        }

        floorRepository.delete(floor);
    }

    private Property findPropertyById(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy property"));
    }
}
