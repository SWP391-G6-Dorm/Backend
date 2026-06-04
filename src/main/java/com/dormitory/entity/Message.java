package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A direct message in a thread between a Tenant and a Landlord.
 * Maps to table: messages
 *
 * FR-NOT-02: history retained for audit compliance (minimum 12 months).
 * threadId groups all messages between the same two participants.
 */
@Entity
@Table(
    name = "messages",
    indexes = {
        @Index(name = "idx_messages_thread",  columnList = "thread_id"),
        @Index(name = "idx_messages_sender",  columnList = "sender_id"),
        @Index(name = "idx_messages_created", columnList = "created_at")
    }
)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Logical thread identifier grouping messages between two participants.
     * Convention: deterministic UUID derived from sorted pair of participant IDs.
     */
    @Column(name = "thread_id", nullable = false)
    private UUID threadId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @NotBlank
    @Column(name = "body", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String body;

    /**
     * JSON array of attachment CDN URLs.
     * e.g. ["https://cdn.example.com/attach/receipt.pdf"]
     */
    @Column(name = "attachments", columnDefinition = "NVARCHAR(MAX)")
    private String attachments;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Message() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getThreadId() { return threadId; }
    public void setThreadId(UUID threadId) { this.threadId = threadId; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
