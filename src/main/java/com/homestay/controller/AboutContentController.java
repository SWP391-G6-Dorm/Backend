package com.homestay.controller;

import com.homestay.dto.request.AboutContentRequest;
import com.homestay.dto.response.AboutContentResponse;
import com.homestay.dto.response.ApiResponse;
import com.homestay.service.AboutContentService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class AboutContentController {

    private final AboutContentService aboutContentService;

    public AboutContentController(AboutContentService aboutContentService) {
        this.aboutContentService = aboutContentService;
    }

    /** Public: trang About */
    @GetMapping("/api/public/about")
    public ResponseEntity<ApiResponse<AboutContentResponse>> getPublicContent() {
        return ResponseEntity.ok(ApiResponse.ok(aboutContentService.getContent()));
    }

    /** Admin: xem nội dung About */
    @GetMapping("/api/admin/about")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AboutContentResponse>> getAdminContent() {
        return ResponseEntity.ok(ApiResponse.ok(aboutContentService.getContent()));
    }

    /** Admin: cập nhật nội dung About (singleton upsert) */
    @PutMapping("/api/admin/about")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AboutContentResponse>> upsert(
            @Valid @RequestBody AboutContentRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trang About thành công", aboutContentService.upsert(req)));
    }

    /** Admin: upload ảnh About từ máy */
    @PostMapping(value = "/api/admin/about/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok("Upload ảnh thành công", aboutContentService.uploadAboutImage(file)));
    }
}
