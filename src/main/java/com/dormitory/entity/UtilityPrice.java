package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Configurable unit price for a utility type (electricity / water).
 * Maps to table: utility_prices
 *
 * Business rules:
 *  - BR-08 / BR-09: only Admin may change global billing tariffs.
 *  - The billing job uses the UtilityPrice with the latest effectiveDate
 *    that is <= the reading date when computing charges.
 */
@Entity
@Table(
    name = "utility_prices",
    indexes = {
        @Index(name = "idx_utilprice_type",    columnList = "utility_type"),
        @Index(name = "idx_utilprice_effdate", columnList = "effective_date")
    }
)
public class UtilityPrice {

    public enum UtilityType { ELECTRICITY, WATER }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "utility_type", nullable = false, length = 20)
    private UtilityType utilityType;

    /**
     * Price per unit (VND).
     * e.g. electricity: 3,500 VND/kWh, water: 10,000 VND/m³
     */
    @NotNull
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    /** Unit label for display purposes (kWh, m³, etc.). */
    @Column(name = "unit_label", nullable = false, length = 20)
    private String unitLabel;

    /** The date from which this price is effective. */
    @NotNull
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    public UtilityPrice() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UtilityType getUtilityType() { return utilityType; }
    public void setUtilityType(UtilityType utilityType) { this.utilityType = utilityType; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public String getUnitLabel() { return unitLabel; }
    public void setUnitLabel(String unitLabel) { this.unitLabel = unitLabel; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
}
