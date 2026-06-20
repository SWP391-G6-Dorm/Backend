package com.homestay.service;

import com.homestay.dto.request.CreateFloorRequest;
import com.homestay.dto.request.UpdateFloorRequest;
import com.homestay.dto.response.FloorResponse;
import com.homestay.dto.response.PropertyStructureResponse;
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

    // ── SCR-37/38: Lấy danh sách tầng của property ───────────────────────────
    public List<FloorResponse> getByProperty(UUID propertyId) {
        Property property = findPropertyById(propertyId);
        return floorRepository.findByPropertyOrderByFloorNumberAsc(property)
                .stream().map(FloorResponse::fromEntity).collect(Collectors.toList());
    }

    // ── SCR-37: Property → Floors → Rooms tree ────────────────────────────────
    @Transactional(readOnly = true)
    public PropertyStructureResponse getStructure(UUID propertyId) {
        // Bước 1: kiểm tra property tồn tại
        Property property = findPropertyById(propertyId);

        // Bước 2: lấy floors kèm rooms bằng 1 query an toàn (tránh MultipleBagFetchException)
        List<com.homestay.entity.Floor> floors = floorRepository.findByPropertyIdWithRooms(propertyId);

        // Bước 3: build response thủ công để tránh lazy load
        PropertyStructureResponse res = new PropertyStructureResponse();
        res.setPropertyId(property.getId());
        res.setPropertyName(property.getName());

        List<PropertyStructureResponse.FloorNode> floorNodes = floors.stream()
                .map(f -> {
                    PropertyStructureResponse.FloorNode fn = new PropertyStructureResponse.FloorNode();
                    fn.setId(f.getId());
                    fn.setFloorNumber(f.getFloorNumber());
                    fn.setDescription(f.getDescription());

                    List<PropertyStructureResponse.RoomNode> roomNodes = (f.getRooms() == null)
                            ? java.util.List.of()
                            : f.getRooms().stream().map(r -> {
                                PropertyStructureResponse.RoomNode rn = new PropertyStructureResponse.RoomNode();
                                rn.setId(r.getId());
                                rn.setRoomNumber(r.getRoomNumber());
                                rn.setRoomType(r.getRoomType());
                                rn.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
                                rn.setPricePerNight(r.getPricePerNight() != null ? r.getPricePerNight().doubleValue() : null);
                                rn.setCapacity(r.getCapacity());
                                return rn;
                            }).collect(Collectors.toList());

                    fn.setRooms(roomNodes);
                    return fn;
                }).collect(Collectors.toList());

        res.setFloors(floorNodes);
        return res;
    }

    // ── SCR-37/38: Tạo tầng mới ───────────────────────────────────────────────
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

    // ── SCR-38: Cập nhật thông tin tầng ──────────────────────────────────────
    @Transactional
    public FloorResponse update(UUID floorId, UpdateFloorRequest request) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tầng"));

        // Kiểm tra floor number mới không trùng với tầng khác trong cùng property
        if (request.getFloorNumber() != null
                && !request.getFloorNumber().equals(floor.getFloorNumber())
                && floorRepository.existsByPropertyAndFloorNumber(floor.getProperty(), request.getFloorNumber())) {
            throw new BusinessException("Tầng " + request.getFloorNumber() + " đã tồn tại trong property này");
        }

        if (request.getFloorNumber() != null) {
            floor.setFloorNumber(request.getFloorNumber());
        }
        if (request.getDescription() != null) {
            floor.setDescription(request.getDescription());
        }

        floorRepository.save(floor);
        return FloorResponse.fromEntity(floor);
    }

    // ── SCR-38: Xóa tầng — không cho xóa nếu còn phòng ─────────────────────
    @Transactional
    public void delete(UUID floorId) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tầng"));

        if (floor.getRooms() != null && !floor.getRooms().isEmpty()) {
            throw new BusinessException("Không thể xóa tầng đang có phòng. Vui lòng xóa phòng trước.");
        }

        floorRepository.delete(floor);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Property findPropertyById(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy property"));
    }
}
