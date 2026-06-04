package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit trail entry for security-sensitive actions.
 * Maps to table: audit_logs
 *
 * NFR-AUD-01: Must be created within the same DB transaction as the triggering action.
 * NFR-AUD-02: Retained for a minimum of 365 days, encrypted at rest.
 *
 * Event types: LOGIN, LOGOUT, CONTRACT_SIGNED, PAYMENT_CONFIRMED, PAYMENT_FAILED,
 * BILLING_ADJUSTMENT, ROLE_CHANGE, MODERATION_ACTION, ROOM_PUBLISHED, USER_SUSPENDED, etc.
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_auditlog_actor",   columnList = "actor_id"),
        @Index(name = "idx_auditlog_created", columnList = "created_at"),
        @Index(name = "idx_auditlog_action",  columnList = "action"),
        @Index(name = "idx_auditlog_entity",  columnList = "entity_type, entity_id")
    }
)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @NotBlank
    @Size(max = 100)
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /**
     * Entity class name affected by the action (spec: entityName).
     * e.g. "Room", "Contract", "Bill", "User"
     */
    @Size(max = 60)
    @Column(name = "entity_name", length = 60)
    private String entityName;

    @Column(name = "entity_id")
    private UUID entityId;

    /** IP address of the actor at the time of the event */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * JSON snapshot of relevant context at the time of the event.
     * e.g. {"oldStatus":"DRAFT","newStatus":"ACTIVE","contractId":"..."}
     */
    @Column(name = "meta", columnDefinition = "NVARCHAR(MAX)")
    private String meta;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getMeta() { return meta; }
    public void setMeta(String meta) { this.meta = meta; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
