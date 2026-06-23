package com.homestay.dto.response;

import com.homestay.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private String roomNumber;
    private String roomType;
    private String propertyName;
    
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal remainingAmount;
    
    private String status;
    private String specialRequests;
    private LocalDateTime createdAt;

    private List<PaymentInfo> payments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private UUID id;
        private String type;
        private BigDecimal amount;
        private String method;
        private String status;
        private LocalDateTime paidAt;

        public static PaymentInfo fromEntity(com.homestay.entity.Payment payment) {
            return new PaymentInfo(
                    payment.getId(),
                    payment.getType().name(),
                    payment.getAmount(),
                    payment.getMethod().name(),
                    payment.getStatus().name(),
                    payment.getPaidAt()
            );
        }
    }

    public static BookingDetailResponse fromEntity(Booking booking) {
        return new BookingDetailResponse(
                booking.getId(),
                booking.getCustomer().getId(),
                booking.getCustomer().getFullName(),
                booking.getCustomer().getEmail(),
                booking.getCustomer().getPhone(),
                booking.getRoom().getRoomNumber(),
                booking.getRoom().getRoomType(),
                booking.getRoom().getProperty().getName(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getGuestCount(),
                booking.getTotalAmount(),
                booking.getDepositAmount(),
                booking.getRemainingAmount(),
                booking.getStatus().name(),
                booking.getSpecialRequests(),
                booking.getCreatedAt(),
                null
        );
    }
}
