package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A utility meter (electricity / water / gas) attached to a Room.
 * Maps to table: utility_meters
 *
 * Business rules:
 *  - BR-06: readings must be non-decreasing; override requires audit note (service layer).
 *  - FR-UTIL-04: missing reading triggers estimated charge (billing job).
 */
@Entity
@Table(
    name = "utility_meters",
    indexes = {
        @Index(name = "idx_utility_room", columnList = "room_id"),
        @Index(name = "idx_utility_type", columnList = "type")
    }
)
public class UtilityMeter {

    public enum Type { ELECTRICITY, WATER, GAS, OTHER }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private Type type;

    /**
     * JSON array of reading records per billing period:
     * [{"date":"2025-06-01","value":1230.5,"photoRef":"https://cdn/...","enteredBy":"uuid"}]
     */
    @Column(name = "readings", columnDefinition = "NVARCHAR(MAX)")
    private String readings;

    /**
     * Tariff rate per unit (VND). Applied by billing job for charge calculation.
     * e.g. electricity: 3,500 VND/kWh
     */
    @NotNull
    @Column(name = "tariff_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal tariffRate;

    /** Unit label for display: kWh, m³, etc. */
    @Column(name = "unit_label", length = 20)
    private String unitLabel;

    public UtilityMeter() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getReadings() { return readings; }
    public void setReadings(String readings) { this.readings = readings; }

    public BigDecimal getTariffRate() { return tariffRate; }
    public void setTariffRate(BigDecimal tariffRate) { this.tariffRate = tariffRate; }

    public String getUnitLabel() { return unitLabel; }
    public void setUnitLabel(String unitLabel) { this.unitLabel = unitLabel; }
}
