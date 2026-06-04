package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A tenant's request to rent a room.
 * Maps to table: rental_requests
 *
 * Business rules enforced at service layer:
 *  - BR: duplicate request suppressed within 24 hours (FR-REQ-04)
 *  - BR: tenants with unpaid bills cannot submit a new request (BR-04)
 */
@Entity
@Table(
    name = "rental_requests",
    indexes = {
        @Index(name = "idx_rentalreq_room",   columnList = "room_id"),
        @Index(name = "idx_rentalreq_tenant", columnList = "tenant_id"),
        @Index(name = "idx_rentalreq_status", columnList = "status")
    }
)
public class RentalRequest {

    public enum Status { PENDING, APPROVED, REJECTED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @NotNull
    @Future
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    /**
     * JSON array of document attachment CDN URLs.
     * e.g. ["https://cdn.example.com/docs/id-card.pdf"]
     */
    @Column(name = "attachments", columnDefinition = "NVARCHAR(MAX)")
    private String attachments;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 1-to-1 contract created after approval */
    @OneToOne(mappedBy = "rentalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Contract contract;

    public RentalRequest() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public User getTenant() { return tenant; }
    public void setTenant(User tenant) { this.tenant = tenant; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public Integer getDurationMonths() { return durationMonths; }
    public void setDurationMonths(Integer durationMonths) { this.durationMonths = durationMonths; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }
}
