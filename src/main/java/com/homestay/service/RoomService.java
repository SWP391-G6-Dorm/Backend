package com.homestay.service;

import com.homestay.dto.request.CreateRoomRequest;
import com.homestay.dto.request.UpdateRoomRequest;
import com.homestay.dto.request.UpdateRoomStatusRequest;
import com.homestay.dto.response.AvailabilityResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.dto.response.RoomDetailResponse;
import com.homestay.dto.response.RoomSummaryResponse;
import com.homestay.entity.Floor;
import com.homestay.entity.Property;
import com.homestay.entity.Room;
import com.homestay.entity.RoomImage;
import com.homestay.exception.BusinessException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.FloorRepository;
import com.homestay.repository.PropertyRepository;
import com.homestay.repository.RoomImageRepository;
import com.homestay.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final FloorRepository floorRepository;
    private final RoomImageRepository roomImageRepository;

    public RoomService(RoomRepository roomRepository,
                       PropertyRepository propertyRepository,
                       FloorRepository floorRepository,
                       RoomImageRepository roomImageRepository) {
        this.roomRepository = roomRepository;
        this.propertyRepository = propertyRepository;
        this.floorRepository = floorRepository;
        this.roomImageRepository = roomImageRepository;
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    // Lấy danh sách phòng (public listing - SCR-07)
    public PageResponse<RoomSummaryResponse> getAll(String search, String status, Pageable pageable) {
        Page<Room> page;

        if (search != null && !search.isBlank()) {
            page = roomRepository.findByRoomNumberContainingIgnoreCaseOrRoomTypeContainingIgnoreCase(
                    search, search, pageable);
        } else if (status != null && !status.isBlank()) {
            page = roomRepository.findByStatus(Room.Status.valueOf(status.toUpperCase()), pageable);
        } else {
            page = roomRepository.findAll(pageable);
        }

        return toPageResponse(page);
    }

    // Lấy danh sách phòng nổi bật cho trang chủ (public - SCR-01)
    public List<RoomSummaryResponse> getFeatured(int limit) {
        Page<Room> page = roomRepository.findByStatus(
                Room.Status.AVAILABLE,
                org.springframework.data.domain.PageRequest.of(0, limit,
                        org.springframework.data.domain.Sort.by("createdAt").descending()));
        return page.getContent().stream().map(RoomSummaryResponse::fromEntity).toList();
    }

    // Lấy chi tiết phòng (public - SCR-08)
    public RoomDetailResponse getById(UUID id) {
        Room room = findById(id);
        return RoomDetailResponse.fromEntity(room);
    }

    // Kiểm tra phòng còn trống không (public - SCR-10)
    public AvailabilityResponse checkAvailability(UUID roomId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new BusinessException("Ngày check-out phải sau ngày check-in");
        }

        boolean hasOverlap = roomRepository.existsOverlapBooking(roomId, checkIn, checkOut);

        // Lấy các khoảng ngày đã đặt
        List<Object[]> ranges = roomRepository.findBookedDateRanges(roomId);
        List<AvailabilityResponse.DateRange> bookedRanges = ranges.stream()
                .map(r -> new AvailabilityResponse.DateRange((LocalDate) r[0], (LocalDate) r[1]))
                .collect(Collectors.toList());

        return new AvailabilityResponse(!hasOverlap, bookedRanges);
    }

    // ── Manager API ────────────────────────────────────────────────────────────

    // Tạo phòng mới
    @Transactional
    public RoomDetailResponse create(CreateRoomRequest request) {
        UUID propertyId = UUID.fromString(request.getPropertyId());
        UUID floorId = UUID.fromString(request.getFloorId());

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy property"));
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tầng"));

        // Floor phải thuộc property
        if (!floor.getProperty().getId().equals(propertyId)) {
            throw new BusinessException("Tầng không thuộc property này");
        }

        Room room = new Room();
        room.setProperty(property);
        room.setFloor(floor);
        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(request.getRoomType());
        room.setPricePerNight(request.getPricePerNight());
        room.setCapacity(request.getCapacity());
        room.setArea(request.getArea());
        room.setDescription(request.getDescription());
        room.setStatus(Room.Status.AVAILABLE);

        roomRepository.save(room);
        return RoomDetailResponse.fromEntity(room);
    }

    // Cập nhật thông tin phòng
    @Transactional
    public RoomDetailResponse update(UUID id, UpdateRoomRequest request) {
        Room room = findById(id);

        if (request.getRoomNumber() != null) room.setRoomNumber(request.getRoomNumber());
        if (request.getRoomType() != null) room.setRoomType(request.getRoomType());
        if (request.getPricePerNight() != null) room.setPricePerNight(request.getPricePerNight());
        if (request.getCapacity() != null) room.setCapacity(request.getCapacity());
        if (request.getArea() != null) room.setArea(request.getArea());
        if (request.getDescription() != null) room.setDescription(request.getDescription());

        roomRepository.save(room);
        return RoomDetailResponse.fromEntity(room);
    }

    // Cập nhật trạng thái phòng (Manager đặt AVAILABLE hoặc MAINTENANCE)
    @Transactional
    public RoomDetailResponse updateStatus(UUID id, UpdateRoomStatusRequest request) {
        Room room = findById(id);

        Room.Status newStatus;
        try {
            newStatus = Room.Status.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Trạng thái không hợp lệ: " + request.getStatus());
        }

        // Manager chỉ được phép set AVAILABLE hoặc MAINTENANCE thủ công
        if (newStatus != Room.Status.AVAILABLE && newStatus != Room.Status.MAINTENANCE) {
            throw new BusinessException("Manager chỉ được đặt trạng thái AVAILABLE hoặc MAINTENANCE");
        }

        room.setStatus(newStatus);
        roomRepository.save(room);
        return RoomDetailResponse.fromEntity(room);
    }

    // Upload ảnh cho phòng
    @Transactional
    public void uploadImages(UUID roomId, List<MultipartFile> files, boolean setPrimary) {
        Room room = findById(roomId);

        // Tạo thư mục nếu chưa có
        String roomUploadDir = uploadDir + "rooms/" + roomId + "/";
        Path dirPath = Paths.get(roomUploadDir);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new BusinessException("Không thể tạo thư mục upload");
        }

        // Lấy sortOrder lớn nhất hiện tại
        List<RoomImage> existingImages = roomImageRepository.findByRoomIdOrderBySortOrderAsc(roomId);
        int nextSort = existingImages.isEmpty() ? 0 : existingImages.get(existingImages.size() - 1).getSortOrder() + 1;

        // Nếu setPrimary và đây là ảnh đầu tiên -> bỏ primary cũ
        if (setPrimary) {
            roomImageRepository.clearPrimaryByRoomId(roomId);
        }

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dirPath.resolve(fileName);

            try {
                Files.write(filePath, file.getBytes());
            } catch (IOException e) {
                throw new BusinessException("Lưu file thất bại: " + file.getOriginalFilename());
            }

            RoomImage image = new RoomImage();
            image.setRoom(room);
            image.setImageUrl("/" + roomUploadDir + fileName);
            image.setIsPrimary(setPrimary && i == 0); // chỉ ảnh đầu là primary
            image.setSortOrder(nextSort + i);
            roomImageRepository.save(image);
        }
    }

    // Xóa ảnh
    @Transactional
    public void deleteImage(UUID imageId) {
        RoomImage image = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh"));
        roomImageRepository.delete(image);
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private Room findById(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));
    }

    private PageResponse<RoomSummaryResponse> toPageResponse(Page<Room> page) {
        return new PageResponse<>(
                page.getContent().stream().map(RoomSummaryResponse::fromEntity).collect(Collectors.toList()),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages()
        );
    }
}
