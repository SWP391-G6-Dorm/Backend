package com.homestay.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AboutContentRequest {

    @NotBlank
    private String heroBrand;

    @NotBlank
    private String heroTitle;

    @NotBlank
    private String heroSubtitle;

    private String heroImageUrl;

    @NotBlank
    private String ctaPrimaryText;

    @NotBlank
    private String ctaPrimaryUrl;

    private String ctaSecondaryText;

    @NotBlank
    private String storyEyebrow;

    @NotBlank
    private String storyTitle;

    @NotBlank
    private String storyBody1;

    @NotBlank
    private String storyBody2;

    private String storyImage1Url;
    private String storyImage2Url;
    private String storyImage3Url;
    private String storyCtaText;
    private String storyCtaUrl;

    @NotBlank
    private String valuesEyebrow;

    @NotBlank
    private String valuesTitle;

    @NotBlank
    private String contactEyebrow;

    @NotBlank
    private String contactTitle;

    @NotBlank
    private String contactIntro;

    @NotBlank
    private String address;

    @NotBlank
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    private String workingHours;

    @NotEmpty
    @Size(min = 1, max = 8)
    @Valid
    private List<AboutStatItem> stats;

    @NotEmpty
    @Size(min = 1, max = 8)
    @Valid
    private List<AboutValueItem> values;
}
