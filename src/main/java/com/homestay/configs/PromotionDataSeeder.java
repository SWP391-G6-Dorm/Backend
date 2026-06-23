package com.homestay.configs;

import com.homestay.entity.Promotion;
import com.homestay.repository.PromotionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * SCR-01 — Seed banner mặc định khi bảng promotions trống (dev/demo).
 * Manager có thể sửa/xóa qua /manager/promotions.
 */
@Component
@Order(20)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = false)
public class PromotionDataSeeder implements ApplicationRunner {

    private final PromotionRepository promotionRepository;

    public PromotionDataSeeder(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (promotionRepository.count() > 0) {
            return;
        }

        promotionRepository.save(build(
                "Ưu đãi cuối tuần",
                "Giảm 20%\nthứ 6 – chủ nhật",
                "Áp dụng cho phòng trống cuối tuần tại tất cả homestay.",
                "Đặt ngay →",
                "/search?sort=price-asc",
                "red",
                0
        ));
        promotionRepository.save(build(
                "Đặt sớm hè 2026",
                "Combo 3 đêm\n+ bữa sáng miễn phí",
                "Ưu đãi có hạn — đặt trước 31/08/2026.",
                "Khám phá →",
                "/search",
                "blue",
                1
        ));
        promotionRepository.save(build(
                "Lưu trú dài hạn",
                "Giảm thêm 15%\ncho booking từ 5 đêm",
                "Lý tưởng cho kỳ nghỉ dài ngày hoặc công tác.",
                "Xem phòng →",
                "/rooms",
                "green",
                2
        ));

        System.out.println("[Seed] Created 3 default promotion banners (SCR-01)");
    }

    private static Promotion build(
            String subtitle, String title, String description,
            String ctaText, String ctaUrl, String colorTheme, int sortOrder) {
        Promotion p = new Promotion();
        p.setSubtitle(subtitle);
        p.setTitle(title);
        p.setDescription(description);
        p.setCtaText(ctaText);
        p.setCtaUrl(ctaUrl);
        p.setColorTheme(colorTheme);
        p.setActive(true);
        p.setSortOrder(sortOrder);
        return p;
    }
}
