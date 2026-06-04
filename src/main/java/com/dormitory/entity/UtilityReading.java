package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A single meter reading record for electricity or water per room per period.
 * Maps to table: utility_readings
 *
 * Business rules:
 *  - BR-06: currentReading must be >= previousReading; a decrease requires
 *            a manual override and an audit note (enforced at service layer).
 *  - FR-UTIL-03: if a reading is missing, an estimated charge is generated
 *                and flagged for correction (billing job, service layer).
 *  - Consumption = currentReading - previousReading.
 *  - Charge = Consumption × UtilityPrice.unitPrice for the matching utility type.
 */
@Entity
@Table(
    name = "utility_readings",
    indexes = {
        @Index(name = "idx_utilread_room",   columnList = "room_id"),
        @Index(name = "idx_utilread_type",   columnList = "utility_type"),
        @Index(name = "idx_utilread_date",   columnList = "reading_date")
    }
)
public class UtilityReading {

    public enum UtilityType { ELECTRICITY, WATER }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "utility_type", nullable = false, length = 20)
    private UtilityType utilityType;

    @NotNull
    @Column(name = "previous_reading", nullable = false, precision = 15, scale = 3)
    private BigDecimal previousReading;

    @NotNull
    @Column(name = "current_reading", nullable = false, precision = 15, scale = 3)
    private BigDecimal currentReading;

    @NotNull
    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    /** CDN URL of the meter photo taken at reading time (optional, for audit). */
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    /**
     * True when the charge was estimated due to a missing reading.
     * Service layer sets this flag and allows correction within 30 days.
     */
    @Column(name = "is_estimated", nullable = false)
    private boolean isEstimated = false;

    /** Landlord/staff who entered this reading. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by")
    private User enteredBy;

    public UtilityReading() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public UtilityType getUtilityType() { return utilityType; }
    public void setUtilityType(UtilityType utilityType) { this.utilityType = utilityType; }

    public BigDecimal getPreviousReading() { return previousReading; }
    public void setPreviousReading(BigDecimal previousReading) { this.previousReading = previousReading; }

    public BigDecimal getCurrentReading() { return currentReading; }
    public void setCurrentReading(BigDecimal currentReading) { this.currentReading = currentReading; }

    public LocalDate getReadingDate() { return readingDate; }
    public void setReadingDate(LocalDate readingDate) { this.readingDate = readingDate; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public boolean isEstimated() { return isEstimated; }
    public void setEstimated(boolean estimated) { isEstimated = estimated; }

    public User getEnteredBy() { return enteredBy; }
    public void setEnteredBy(User enteredBy) { this.enteredBy = enteredBy; }
}
