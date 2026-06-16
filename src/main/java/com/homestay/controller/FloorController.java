package com.homestay.controller;

import com.homestay.dto.request.CreateFloorRequest;
import com.homestay.dto.response.ApiResponse;
import com.homestay.dto.response.FloorResponse;
import com.homestay.service.FloorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/floors")
public class FloorController {

    private final FloorService floorService;

    public FloorController(FloorService floorService) {
        this.floorService = floorService;
    }

    // Lấy danh sách tầng của property - public
    @GetMapping
    public ResponseEntity<ApiResponse<List<FloorResponse>>> getByProperty(
            @RequestParam UUID propertyId) {

        return ResponseEntity.ok(ApiResponse.ok(floorService.getByProperty(propertyId)));
    }

    // Tạo tầng mới - chỉ Manager
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<FloorResponse>> create(
            @Valid @RequestBody CreateFloorRequest request) {

        FloorResponse res = floorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Tạo tầng thành công", res));
    }

    // Xóa tầng - chỉ Manager
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        floorService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa tầng thành công"));
    }
}
