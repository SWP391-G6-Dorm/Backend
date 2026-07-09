package com.homestay.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** SCR-64 - Create Damage Report body from Employee. */
@Data
public class CreateEmployeeDamageReportRequest {

    @NotNull
    private UUID roomId;

    @NotEmpty
    @Valid
    private List<Item> items;

    @Valid
    private List<AttachmentRef> attachments;

    private String notes;

    @Data
    public static class Item {
        @NotBlank
        private String name;

        @NotNull
        @Positive
        private BigDecimal estimatedCost;
    }

    @Data
    public static class AttachmentRef {
        @NotBlank
        private String url;

        private String type;
    }
}