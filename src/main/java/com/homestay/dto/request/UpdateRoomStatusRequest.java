package com.homestay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoomStatusRequest {
    // AVAILABLE, MAINTENANCE (Manager đặt thủ công)
    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
}
