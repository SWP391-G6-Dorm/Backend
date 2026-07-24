package com.homestay.service;

import com.homestay.dto.request.PromotionRequest;
import com.homestay.dto.response.PromotionResponse;
import com.homestay.entity.Promotion;
import com.homestay.exception.BusinessException;
import com.homestay.exception.ResourceNotFoundException;
import com.homestay.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private static final long MAX_BANNER_BYTES = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    private final PromotionRepository promotionRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    /** Public: chỉ lấy banner đang active */
    @Transactional(readOnly = true)
    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream().map(PromotionResponse::fromEntity).collect(Collectors.toList());
    }

    /** Manager: lấy tất cả kể cả inactive */
    @Transactional(readOnly = true)
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAllByOrderBySortOrderAsc()
                .stream().map(PromotionResponse::fromEntity).collect(Collectors.toList());
    }

    /** Manager: tạo mới */
    @Transactional
    public PromotionResponse create(PromotionRequest req) {
        Promotion p = new Promotion();
        applyRequest(p, req);
        return PromotionResponse.fromEntity(promotionRepository.save(p));
    }

    /** Manager: cập nhật */
    @Transactional
    public PromotionResponse update(UUID id, PromotionRequest req) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner không tồn tại"));
        applyRequest(p, req);
        return PromotionResponse.fromEntity(promotionRepository.save(p));
    }

    /** Manager: xóa */
    @Transactional
    public void delete(UUID id) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner không tồn tại"));
        promotionRepository.delete(p);
    }

    /**
     * Manager: upload ảnh banner → lưu uploads/banners/ và trả URL public.
     */
    public Map<String, String> uploadBannerImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Vui lòng chọn file ảnh");
        }
        if (file.getSize() > MAX_BANNER_BYTES) {
            throw new BusinessException("Ảnh banner tối đa 50MB");
        }
        String contentType = file.getContentType() != null
                ? file.getContentType().toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("Chỉ chấp nhận ảnh JPEG, PNG, WebP hoặc GIF");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "banner.jpg";
        }
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalName.substring(dot).toLowerCase(Locale.ROOT);
        }
        if (extension.isBlank()) {
            extension = contentType.contains("png") ? ".png"
                    : contentType.contains("webp") ? ".webp"
                    : contentType.contains("gif") ? ".gif"
                    : ".jpg";
        }

        String bannerDir = uploadDir + "banners/";
        Path dirPath = Paths.get(bannerDir);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new BusinessException("Không thể tạo thư mục upload banner");
        }

        String fileName = UUID.randomUUID() + extension;
        Path filePath = dirPath.resolve(fileName);
        try {
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new BusinessException("Lưu ảnh banner thất bại");
        }

        String imageUrl = "/uploads/banners/" + fileName;
        return Map.of("imageUrl", imageUrl);
    }

    private void applyRequest(Promotion p, PromotionRequest req) {
        p.setSubtitle(req.getSubtitle());
        p.setTitle(req.getTitle());
        p.setDescription(req.getDescription());
        p.setCtaText(req.getCtaText());
        p.setCtaUrl(req.getCtaUrl());
        p.setImageUrl(req.getImageUrl());
        p.setColorTheme(req.getColorTheme());
        p.setActive(req.getIsActive() != null ? req.getIsActive() : true);
        p.setSortOrder(req.getSortOrder());
    }
}
