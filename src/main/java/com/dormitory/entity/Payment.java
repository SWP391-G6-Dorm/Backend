package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A payment transaction linked to a Bill.
 * Maps to table: payments
 *
 * Business rules:
 *  - FR-PAY-03: providerResponse is stored raw; HMAC signature verified BEFORE
 *               any status change (enforced at service layer, not here).
 *  - BR-09: state mutation only after signature verification.
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_bill",   columnList = "bill_id"),
        @Index(name = "idx_payments_status", columnList = "status"),
        @Index(name = "idx_payments_ref",    columnList = "transaction_ref")
    }
)
public class Payment {

    public enum Method { VNPAY, CARD, BANK_TRANSFER, CASH, E_WALLET }
    public enum Status { PENDING, SUCCESS, FAILED, REFUNDED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private Method method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    /** Raw JSON payload from the payment provider callback */
    @Column(name = "provider_response", columnDefinition = "NVARCHAR(MAX)")
    private String providerResponse;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Receipt uploaded by tenant for offline/manual payments (optional). */
    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private PaymentReceipt receipt;

    public Payment() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Method getMethod() { return method; }
    public void setMethod(Method method) { this.method = method; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public PaymentReceipt getReceipt() { return receipt; }
    public void setReceipt(PaymentReceipt receipt) { this.receipt = receipt; }
}
