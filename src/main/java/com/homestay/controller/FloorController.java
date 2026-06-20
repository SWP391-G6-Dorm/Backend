package com.homestay.controller;

import com.homestay.dto.request.CreateFloorRequest;
import com.homestay.dto.request.UpdateFloorRequest;
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

    // SCR-37/38: Lấy danh sách tầng của property
    @GetMapping
    public ResponseEntity<ApiResponse<List<FloorResponse>>> getByProperty(
            @RequestParam UUID propertyId) {

        return ResponseEntity.ok(ApiResponse.ok(floorService.getByProperty(propertyId)));
    }

    // SCR-37/38: Tạo tầng mới — chỉ Manager
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<FloorResponse>> create(
            @Valid @RequestBody CreateFloorRequest request) {

        FloorResponse res = floorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Tạo tầng thành công", res));
    }

    // SCR-38: Cập nhật tầng — chỉ Manager
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<FloorResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFloorRequest request) {

        FloorResponse res = floorService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật tầng thành công", res));
    }

    // SCR-38: Xóa tầng — chỉ Manager
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        floorService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa tầng thành công"));
    }
}
