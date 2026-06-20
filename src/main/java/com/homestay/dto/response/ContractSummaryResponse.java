package com.homestay.dto.response;

import com.homestay.entity.Contract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractSummaryResponse {
    private UUID id;
    private UUID bookingId;
    private String customerName;
    private String customerEmail;
    private String roomNumber;
    private String propertyName;
    private BigDecimal totalAmount;
    private LocalDateTime generatedAt;
    private String status;

    public static ContractSummaryResponse fromEntity(Contract c) {
        return new ContractSummaryResponse(
                c.getId(),
                c.getBooking().getId(),
                c.getCustomer().getFullName() != null ? c.getCustomer().getFullName() : "Unknown",
                c.getCustomer().getEmail(),
                c.getRoom().getRoomNumber(),
                c.getRoom().getProperty().getName(),
                c.getTotalAmount(),
                c.getGeneratedAt(),
                c.getStatus().name()
        );
    }
}
