package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * An in-app notification delivered to a specific user.
 * Maps to table: notifications
 *
 * readAt = NULL means unread.
 * Notification types (FR-NOT-01): PAYMENT_DUE, PAYMENT_OVERDUE, PAYMENT_CONFIRMED,
 * PAYMENT_FAILED, REQUEST_RECEIVED, REQUEST_APPROVED, REQUEST_REJECTED,
 * CONTRACT_READY, CONTRACT_SIGNED, TICKET_UPDATE, ANNOUNCEMENT, SYSTEM.
 */
@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notif_recipient", columnList = "recipient_id, read_at"),
        @Index(name = "idx_notif_created",   columnList = "created_at")
    }
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @NotBlank
    @Size(max = 60)
    @Column(name = "type", nullable = false, length = 60)
    private String type;

    /**
     * JSON payload specific to notification type.
     * e.g. {"billId":"...","amount":3500000,"dueDate":"2025-06-15"}
     */
    @Column(name = "payload", columnDefinition = "NVARCHAR(MAX)")
    private String payload;

    /** NULL = unread; set to current timestamp when user reads the notification */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Convenience method */
    public boolean isRead() { return readAt != null; }
}
