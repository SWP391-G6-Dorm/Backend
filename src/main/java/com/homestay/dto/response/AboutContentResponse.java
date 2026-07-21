package com.homestay.dto.response;

import com.homestay.dto.request.AboutStatItem;
import com.homestay.dto.request.AboutValueItem;
import com.homestay.entity.AboutContent;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AboutContentResponse {
    private UUID id;
    private String heroBrand;
    private String heroTitle;
    private String heroSubtitle;
    private String heroImageUrl;
    private String ctaPrimaryText;
    private String ctaPrimaryUrl;
    private String ctaSecondaryText;
    private String storyEyebrow;
    private String storyTitle;
    private String storyBody1;
    private String storyBody2;
    private String storyImage1Url;
    private String storyImage2Url;
    private String storyImage3Url;
    private String storyCtaText;
    private String storyCtaUrl;
    private String valuesEyebrow;
    private String valuesTitle;
    private String contactEyebrow;
    private String contactTitle;
    private String contactIntro;
    private String address;
    private String email;
    private String phone;
    private String workingHours;
    private List<AboutStatItem> stats;
    private List<AboutValueItem> values;
    private LocalDateTime updatedAt;

    public static AboutContentResponse fromEntity(
            AboutContent entity,
            List<AboutStatItem> stats,
            List<AboutValueItem> values) {
        AboutContentResponse r = new AboutContentResponse();
        r.setId(entity.getId());
        r.setHeroBrand(entity.getHeroBrand());
        r.setHeroTitle(entity.getHeroTitle());
        r.setHeroSubtitle(entity.getHeroSubtitle());
        r.setHeroImageUrl(entity.getHeroImageUrl());
        r.setCtaPrimaryText(entity.getCtaPrimaryText());
        r.setCtaPrimaryUrl(entity.getCtaPrimaryUrl());
        r.setCtaSecondaryText(entity.getCtaSecondaryText());
        r.setStoryEyebrow(entity.getStoryEyebrow());
        r.setStoryTitle(entity.getStoryTitle());
        r.setStoryBody1(entity.getStoryBody1());
        r.setStoryBody2(entity.getStoryBody2());
        r.setStoryImage1Url(entity.getStoryImage1Url());
        r.setStoryImage2Url(entity.getStoryImage2Url());
        r.setStoryImage3Url(entity.getStoryImage3Url());
        r.setStoryCtaText(entity.getStoryCtaText());
        r.setStoryCtaUrl(entity.getStoryCtaUrl());
        r.setValuesEyebrow(entity.getValuesEyebrow());
        r.setValuesTitle(entity.getValuesTitle());
        r.setContactEyebrow(entity.getContactEyebrow());
        r.setContactTitle(entity.getContactTitle());
        r.setContactIntro(entity.getContactIntro());
        r.setAddress(entity.getAddress());
        r.setEmail(entity.getEmail());
        r.setPhone(entity.getPhone());
        r.setWorkingHours(entity.getWorkingHours());
        r.setStats(stats);
        r.setValues(values);
        r.setUpdatedAt(entity.getUpdatedAt());
        return r;
    }
}
