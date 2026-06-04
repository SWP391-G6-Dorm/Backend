package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A user complaint or flag against a listing, user, or review.
 * Maps to table: complaints
 *
 * FR-CM-01: Admin can Remove, Suspend, Require edits, or Dismiss.
 * All moderation actions are stored in the JSON actions array with moderatorId + reason.
 */
@Entity
@Table(
    name = "complaints",
    indexes = {
        @Index(name = "idx_complaints_reporter",    columnList = "reporter_id"),
        @Index(name = "idx_complaints_status",      columnList = "status"),
        @Index(name = "idx_complaints_target_type", columnList = "target_type, target_id")
    }
)
public class Complaint {

    public enum TargetType { LISTING, USER, REVIEW }
    public enum Status { OPEN, UNDER_REVIEW, RESOLVED, DISMISSED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    /** UUID of the targeted entity (Room.id / User.id / Review.id) */
    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @NotBlank
    @Size(max = 60)
    @Column(name = "category", nullable = false, length = 60)
    private String category;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.OPEN;

    /**
     * JSON array of moderation action records:
     * [{"moderatorId":"...","action":"REMOVE","reason":"Spam listing","at":"..."}]
     */
    @Column(name = "actions", columnDefinition = "NVARCHAR(MAX)")
    private String actions;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Complaint() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getActions() { return actions; }
    public void setActions(String actions) { this.actions = actions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
