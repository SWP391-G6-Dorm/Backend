package com.homestay.dto.response;

import com.homestay.entity.Complaint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SCR-54 - Complaint Management (Admin). Field khop FE AdminComplaint.
 * bookingId luon "" vi entity Complaint khong co booking (FE goi .slice).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminComplaintResponse {

    private String id;
    private String customerId;
    private String customerName;
    private String bookingId;
    private String description;
    private String status;
    private String resolution;
    private LocalDateTime createdAt;

    public static AdminComplaintResponse from(Complaint c) {
        String customerId = "";
        String customerName = "Khach vang lai";
        if (c.getUser() != null) {
            customerId = c.getUser().getId().toString();
            customerName = c.getUser().getFullName();
        }
        return AdminComplaintResponse.builder()
                .id(c.getId().toString())
                .customerId(customerId)
                .customerName(customerName)
                .bookingId("")
                .description(c.getDescription())
                .status(c.getStatus().name())
                .resolution(c.getResolutionNotes())
                .createdAt(c.getCreatedAt())
                .build();
    }
}