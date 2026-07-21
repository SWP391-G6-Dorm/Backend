package com.homestay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "about_content")
@Getter
@Setter
@NoArgsConstructor
public class AboutContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 40, columnDefinition = "NVARCHAR(40)")
    private String singletonKey = "default";

    // Hero
    @Column(nullable = false, length = 120, columnDefinition = "NVARCHAR(120)")
    private String heroBrand;

    @Column(nullable = false, length = 300, columnDefinition = "NVARCHAR(300)")
    private String heroTitle;

    @Column(nullable = false, length = 600, columnDefinition = "NVARCHAR(600)")
    private String heroSubtitle;

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String heroImageUrl;

    @Column(nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String ctaPrimaryText;

    @Column(nullable = false, length = 300, columnDefinition = "NVARCHAR(300)")
    private String ctaPrimaryUrl;

    @Column(length = 100, columnDefinition = "NVARCHAR(100)")
    private String ctaSecondaryText;

    // Story
    @Column(nullable = false, length = 120, columnDefinition = "NVARCHAR(120)")
    private String storyEyebrow;

    @Column(nullable = false, length = 300, columnDefinition = "NVARCHAR(300)")
    private String storyTitle;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String storyBody1;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String storyBody2;

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String storyImage1Url;

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String storyImage2Url;

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String storyImage3Url;

    @Column(length = 100, columnDefinition = "NVARCHAR(100)")
    private String storyCtaText;

    @Column(length = 300, columnDefinition = "NVARCHAR(300)")
    private String storyCtaUrl;

    // Values header
    @Column(nullable = false, length = 120, columnDefinition = "NVARCHAR(120)")
    private String valuesEyebrow;

    @Column(nullable = false, length = 300, columnDefinition = "NVARCHAR(300)")
    private String valuesTitle;

    // Contact
    @Column(nullable = false, length = 120, columnDefinition = "NVARCHAR(120)")
    private String contactEyebrow;

    @Column(nullable = false, length = 300, columnDefinition = "NVARCHAR(300)")
    private String contactTitle;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String contactIntro;

    @Column(nullable = false, length = 300, columnDefinition = "NVARCHAR(300)")
    private String address;

    @Column(nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String email;

    @Column(nullable = false, length = 80, columnDefinition = "NVARCHAR(80)")
    private String phone;

    @Column(nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String workingHours;

    /** JSON array: [{ value, label }] */
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String statsJson;

    /** JSON array: [{ num, title, desc }] */
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String valuesJson;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
