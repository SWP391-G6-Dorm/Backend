package com.homestay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homestay.dto.request.AboutContentRequest;
import com.homestay.dto.request.AboutStatItem;
import com.homestay.dto.request.AboutValueItem;
import com.homestay.dto.response.AboutContentResponse;
import com.homestay.entity.AboutContent;
import com.homestay.exception.BusinessException;
import com.homestay.repository.AboutContentRepository;
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

@Service
public class AboutContentService {

    private static final String SINGLETON_KEY = "default";
    private static final long MAX_IMAGE_BYTES = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    private final AboutContentRepository aboutContentRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public AboutContentService(AboutContentRepository aboutContentRepository, ObjectMapper objectMapper) {
        this.aboutContentRepository = aboutContentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public AboutContentResponse getContent() {
        AboutContent entity = getOrSeedSingleton();
        return toResponse(entity);
    }

    @Transactional
    public AboutContentResponse upsert(AboutContentRequest req) {
        AboutContent entity = aboutContentRepository.findBySingletonKey(SINGLETON_KEY)
                .orElseGet(this::createDefaultEntity);
        applyRequest(entity, req);
        return toResponse(aboutContentRepository.save(entity));
    }

    public Map<String, String> uploadAboutImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Vui lòng chọn file ảnh");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new BusinessException("Ảnh tối đa 50MB");
        }
        String contentType = file.getContentType() != null
                ? file.getContentType().toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("Chỉ chấp nhận ảnh JPEG, PNG, WebP hoặc GIF");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "about.jpg";
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

        String aboutDir = uploadDir + "about/";
        Path dirPath = Paths.get(aboutDir);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new BusinessException("Không thể tạo thư mục upload about");
        }

        String fileName = UUID.randomUUID() + extension;
        Path filePath = dirPath.resolve(fileName);
        try {
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new BusinessException("Lưu ảnh about thất bại");
        }

        return Map.of("imageUrl", "/uploads/about/" + fileName);
    }

    @Transactional
    protected AboutContent getOrSeedSingleton() {
        return aboutContentRepository.findBySingletonKey(SINGLETON_KEY)
                .orElseGet(() -> aboutContentRepository.save(createDefaultEntity()));
    }

    private AboutContent createDefaultEntity() {
        AboutContent entity = new AboutContent();
        entity.setSingletonKey(SINGLETON_KEY);
        entity.setHeroBrand("Homestay&Resort");
        entity.setHeroTitle("Kết nối du khách với những kỳ nghỉ đáng nhớ");
        entity.setHeroSubtitle(
                "Nền tảng đặt phòng homestay & resort tin cậy tại Việt Nam — tìm phòng, "
                        + "đặt cọc, hợp đồng điện tử và thanh toán, tất cả ở một nơi.");
        entity.setHeroImageUrl(
                "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=1600&q=80");
        entity.setCtaPrimaryText("Khám phá phòng");
        entity.setCtaPrimaryUrl("/rooms");
        entity.setCtaSecondaryText("Liên hệ với chúng tôi");
        entity.setStoryEyebrow("CÂU CHUYỆN CỦA CHÚNG TÔI");
        entity.setStoryTitle("Bắt đầu từ trăn trở của một người lữ hành");
        entity.setStoryBody1(
                "Thành lập năm 2023, Homestay&Resort ra đời khi những người sáng lập "
                        + "gặp khó khăn trong việc tìm chỗ ở tin cậy tại Việt Nam — thông tin rời rạc, "
                        + "không có hợp đồng điện tử, giá cả thiếu minh bạch.");
        entity.setStoryBody2(
                "Hôm nay, chúng tôi phục vụ hàng nghìn du khách và chủ nhà đã xác minh tại "
                        + "Đà Nẵng, Đà Lạt, Hội An, Phú Quốc và Nha Trang — từ tìm phòng đến thanh toán "
                        + "và hợp đồng, tất cả trong một nền tảng.");
        entity.setStoryImage1Url(
                "https://images.unsplash.com/photo-1540541338287-41700207dee6?w=800&q=80");
        entity.setStoryImage2Url(
                "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=500&q=80");
        entity.setStoryImage3Url(
                "https://images.unsplash.com/photo-1602002418082-a4443e081dd1?w=500&q=80");
        entity.setStoryCtaText("Xem các điểm đến →");
        entity.setStoryCtaUrl("/rooms");
        entity.setValuesEyebrow("GIÁ TRỊ CỐT LÕI");
        entity.setValuesTitle("Điều chúng tôi cam kết trong từng kỳ nghỉ");
        entity.setContactEyebrow("LIÊN HỆ");
        entity.setContactTitle("Chúng tôi luôn sẵn sàng lắng nghe");
        entity.setContactIntro(
                "Bạn có câu hỏi, khiếu nại hay đề xuất hợp tác? Đội ngũ của chúng tôi "
                        + "thường phản hồi trong vòng 24 giờ làm việc.");
        entity.setAddress("125 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh");
        entity.setEmail("support@homestay-resort.vn");
        entity.setPhone("+84 28 1234 5678");
        entity.setWorkingHours("Thứ 2 – Thứ 6: 8:00 – 17:30 (ICT)");
        entity.setStatsJson(defaultStatsJson());
        entity.setValuesJson(defaultValuesJson());
        return entity;
    }

    private String defaultStatsJson() {
        try {
            return objectMapper.writeValueAsString(List.of(
                    stat("2023", "Năm thành lập"),
                    stat("5+", "Thành phố"),
                    stat("5.000+", "Khách hài lòng"),
                    stat("98%", "Đánh giá tích cực")
            ));
        } catch (JsonProcessingException e) {
            throw new BusinessException("Không thể tạo dữ liệu stats mặc định");
        }
    }

    private String defaultValuesJson() {
        try {
            return objectMapper.writeValueAsString(List.of(
                    value("01", "Tin cậy",
                            "Mọi cơ sở lưu trú đều được xác minh. Mọi thông tin phòng đều chính xác, minh bạch."),
                    value("02", "Minh bạch",
                            "Không phí ẩn. Giá phòng, tiền cọc và chính sách hủy rõ ràng ngay từ đầu."),
                    value("03", "Nhanh chóng",
                            "Từ tìm phòng đến nhận phòng chỉ trong 24 giờ — hợp đồng điện tử, thanh toán trực tuyến."),
                    value("04", "Công bằng",
                            "Khiếu nại và tranh chấp được xử lý chuyên nghiệp, đặt quyền lợi khách hàng lên trước.")
            ));
        } catch (JsonProcessingException e) {
            throw new BusinessException("Không thể tạo dữ liệu values mặc định");
        }
    }

    private AboutStatItem stat(String value, String label) {
        AboutStatItem item = new AboutStatItem();
        item.setValue(value);
        item.setLabel(label);
        return item;
    }

    private AboutValueItem value(String num, String title, String desc) {
        AboutValueItem item = new AboutValueItem();
        item.setNum(num);
        item.setTitle(title);
        item.setDesc(desc);
        return item;
    }

    private void applyRequest(AboutContent entity, AboutContentRequest req) {
        entity.setHeroBrand(req.getHeroBrand());
        entity.setHeroTitle(req.getHeroTitle());
        entity.setHeroSubtitle(req.getHeroSubtitle());
        entity.setHeroImageUrl(req.getHeroImageUrl());
        entity.setCtaPrimaryText(req.getCtaPrimaryText());
        entity.setCtaPrimaryUrl(req.getCtaPrimaryUrl());
        entity.setCtaSecondaryText(req.getCtaSecondaryText());
        entity.setStoryEyebrow(req.getStoryEyebrow());
        entity.setStoryTitle(req.getStoryTitle());
        entity.setStoryBody1(req.getStoryBody1());
        entity.setStoryBody2(req.getStoryBody2());
        entity.setStoryImage1Url(req.getStoryImage1Url());
        entity.setStoryImage2Url(req.getStoryImage2Url());
        entity.setStoryImage3Url(req.getStoryImage3Url());
        entity.setStoryCtaText(req.getStoryCtaText());
        entity.setStoryCtaUrl(req.getStoryCtaUrl());
        entity.setValuesEyebrow(req.getValuesEyebrow());
        entity.setValuesTitle(req.getValuesTitle());
        entity.setContactEyebrow(req.getContactEyebrow());
        entity.setContactTitle(req.getContactTitle());
        entity.setContactIntro(req.getContactIntro());
        entity.setAddress(req.getAddress());
        entity.setEmail(req.getEmail());
        entity.setPhone(req.getPhone());
        entity.setWorkingHours(req.getWorkingHours());
        entity.setStatsJson(writeJson(req.getStats()));
        entity.setValuesJson(writeJson(req.getValues()));
    }

    private AboutContentResponse toResponse(AboutContent entity) {
        return AboutContentResponse.fromEntity(
                entity,
                readStats(entity.getStatsJson()),
                readValues(entity.getValuesJson())
        );
    }

    private List<AboutStatItem> readStats(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<AboutStatItem>>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException("Dữ liệu stats không hợp lệ");
        }
    }

    private List<AboutValueItem> readValues(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<AboutValueItem>>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException("Dữ liệu values không hợp lệ");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Không thể lưu dữ liệu JSON");
        }
    }
}
