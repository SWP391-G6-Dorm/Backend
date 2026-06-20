package com.homestay.dto.response;

import com.homestay.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
                booking.getCreatedAt()
        );
    }
}
