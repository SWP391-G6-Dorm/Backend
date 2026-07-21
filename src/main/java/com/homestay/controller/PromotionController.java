package com.homestay.controller;

import com.homestay.dto.request.PromotionRequest;
import com.homestay.dto.response.ApiResponse;
import com.homestay.dto.response.PromotionResponse;
import com.homestay.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    /** Public: Landing Page lấy banner active */
    @GetMapping("/api/public/promotions")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.ok(promotionService.getActivePromotions()));
    }

    /** Admin: xem tất cả banner (kể cả inactive) */
    @GetMapping("/api/admin/banners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(promotionService.getAllPromotions()));
    }

    /** Admin: upload ảnh banner từ máy */
    @PostMapping(value = "/api/admin/banners/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Upload ảnh thành công", promotionService.uploadBannerImage(file)));
    }

    /** Admin: tạo banner mới */
    @PostMapping("/api/admin/banners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> create(
            @Valid @RequestBody PromotionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo banner thành công", promotionService.create(req)));
    }

    /** Admin: cập nhật banner */
    @PutMapping("/api/admin/banners/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PromotionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thành công", promotionService.update(id, req)));
    }

    /** Admin: xóa banner */
    @DeleteMapping("/api/admin/banners/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        promotionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa banner thành công"));
    }
}
