package com.homestay.controller;

import com.homestay.dto.request.CreateRoomRequest;
import com.homestay.dto.request.UpdateRoomRequest;
import com.homestay.dto.request.UpdateRoomStatusRequest;
import com.homestay.dto.response.ApiResponse;
import com.homestay.dto.response.AvailabilityResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.dto.response.RoomDetailResponse;
import com.homestay.dto.response.RoomSummaryResponse;
import com.homestay.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // Danh sách phòng - public (SCR-07, SCR-09)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RoomSummaryResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(roomService.getAll(search, status, pageable)));
    }

    // Chi tiết phòng - public (SCR-08)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(roomService.getById(id)));
    }

    // Kiểm tra availability - public (SCR-10)
    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        return ResponseEntity.ok(ApiResponse.ok(roomService.checkAvailability(id, checkIn, checkOut)));
    }

    // Tạo phòng - chỉ Manager
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> create(
            @Valid @RequestBody CreateRoomRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo phòng thành công", roomService.create(request)));
    }

    // Cập nhật phòng - chỉ Manager
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> update(
            @PathVariable UUID id,
            @RequestBody UpdateRoomRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật phòng thành công", roomService.update(id, request)));
    }

    // Cập nhật trạng thái phòng - chỉ Manager (đặt AVAILABLE / MAINTENANCE)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoomStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", roomService.updateStatus(id, request)));
    }

    // Upload ảnh phòng - chỉ Manager (SCR-43)
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> uploadImages(
            @PathVariable UUID id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "false") boolean setPrimary) {

        roomService.uploadImages(id, files, setPrimary);
        return ResponseEntity.ok(ApiResponse.ok("Upload ảnh thành công"));
    }

    // Xóa ảnh phòng - chỉ Manager
    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable UUID imageId) {
        roomService.deleteImage(imageId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa ảnh thành công"));
    }
}
