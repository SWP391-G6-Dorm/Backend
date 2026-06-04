package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A maintenance / repair ticket submitted by a Tenant.
 * Maps to table: maintenance_tickets
 *
 * Business rules:
 *  - FR-MT-03: EMERGENCY tickets trigger SLA escalation to on-call staff (billing job/event).
 *  - slaDeadline is computed by service: EMERGENCY=+2h, HIGH=+24h, MEDIUM=+72h, LOW=+168h.
 */
@Entity
@Table(
    name = "maintenance_tickets",
    indexes = {
        @Index(name = "idx_mt_room",     columnList = "room_id"),
        @Index(name = "idx_mt_reporter", columnList = "reporter_id"),
        @Index(name = "idx_mt_status",   columnList = "status"),
        @Index(name = "idx_mt_priority", columnList = "priority")
    }
)
public class MaintenanceTicket {

    public enum Priority { LOW, MEDIUM, HIGH, EMERGENCY }
    public enum Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @NotBlank
    @Size(max = 60)
    @Column(name = "category", nullable = false, length = 60)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority;

    @NotBlank
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String description;

    /**
     * JSON array of photo CDN URLs submitted with the ticket.
     */
    @Column(name = "photo_urls", columnDefinition = "NVARCHAR(MAX)")
    private String photoUrls;

    /**
     * JSON array of comment records:
     * [{"userId":"...","body":"...","createdAt":"..."}]
     */
    @Column(name = "comments", columnDefinition = "NVARCHAR(MAX)")
    private String comments;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.OPEN;

    /** Computed SLA deadline; null until ticket is created with priority */
    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MaintenanceTicket() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public User getAssignee() { return assignee; }
    public void setAssignee(User assignee) { this.assignee = assignee; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
