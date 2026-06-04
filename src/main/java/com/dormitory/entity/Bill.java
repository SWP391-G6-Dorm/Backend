package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * An invoice generated against a Contract.
 * Maps to table: bills
 *
 * Business rules:
 *  - BR-02: dueDate >= issueDate (enforced by @AssertTrue at service/DTO level).
 *  - Only UNPAID bills may initiate payment (enforced at service layer).
 */
@Entity
@Table(
    name = "bills",
    indexes = {
        @Index(name = "idx_bills_contract", columnList = "contract_id"),
        @Index(name = "idx_bills_status",   columnList = "status"),
        @Index(name = "idx_bills_due_date", columnList = "due_date")
    }
)
public class Bill {

    public enum Status { PENDING, PAID, OVERDUE, DISPUTED, WAIVED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** Billing month label, e.g. "2025-06" (YYYY-MM). */
    @Column(name = "billing_period", length = 7)
    private String billingPeriod;

    @Column(name = "room_rent", precision = 15, scale = 2)
    private BigDecimal roomRent;

    @Column(name = "electricity_fee", precision = 15, scale = 2)
    private BigDecimal electricityFee;

    @Column(name = "water_fee", precision = 15, scale = 2)
    private BigDecimal waterFee;

    @Column(name = "service_fee", precision = 15, scale = 2)
    private BigDecimal serviceFee;

    /** Pre-computed total = roomRent + electricityFee + waterFee + serviceFee. */
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    /**
     * JSON array of line items:
     * [{"label":"Rent","amount":3500000},{"label":"Electricity (Estimated)","amount":250000,"estimated":true}]
     */
    @Column(name = "line_items", columnDefinition = "NVARCHAR(MAX)")
    private String lineItems;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments;

    public Bill() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getBillingPeriod() { return billingPeriod; }
    public void setBillingPeriod(String billingPeriod) { this.billingPeriod = billingPeriod; }

    public BigDecimal getRoomRent() { return roomRent; }
    public void setRoomRent(BigDecimal roomRent) { this.roomRent = roomRent; }

    public BigDecimal getElectricityFee() { return electricityFee; }
    public void setElectricityFee(BigDecimal electricityFee) { this.electricityFee = electricityFee; }

    public BigDecimal getWaterFee() { return waterFee; }
    public void setWaterFee(BigDecimal waterFee) { this.waterFee = waterFee; }

    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getLineItems() { return lineItems; }
    public void setLineItems(String lineItems) { this.lineItems = lineItems; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }
}
