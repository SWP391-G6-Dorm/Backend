package com.homestay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRoomStatusRequest {

    // AVAILABLE, MAINTENANCE (Manager đặt thủ công)
    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    // Ghi chú lý do — khuyến nghị khi chọn MAINTENANCE
    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}
