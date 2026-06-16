package com.homestay.controller;

import com.homestay.dto.request.CreatePropertyRequest;
import com.homestay.dto.request.UpdatePropertyRequest;
import com.homestay.dto.response.ApiResponse;
import com.homestay.dto.response.PageResponse;
import com.homestay.dto.response.PropertyResponse;
import com.homestay.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    // Lấy danh sách property - public (SCR-01, SCR-07)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PropertyResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(propertyService.getAll(search, status, pageable)));
    }

    // Xem chi tiết property - public
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(propertyService.getById(id)));
    }

    // Tạo property - chỉ Manager
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PropertyResponse>> create(
            @Valid @RequestBody CreatePropertyRequest request) {

        PropertyResponse res = propertyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Tạo property thành công", res));
    }

    // Cập nhật property - chỉ Manager
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PropertyResponse>> update(
            @PathVariable UUID id,
            @RequestBody UpdatePropertyRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật property thành công", propertyService.update(id, request)));
    }

    // Xóa property - chỉ Manager
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        propertyService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa property thành công"));
    }
}
