package com.homestay.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotNull(message = "Mã đặt phòng không được để trống")
    private UUID bookingId;

    @NotNull(message = "Số sao đánh giá không được để trống")
    @Min(value = 1, message = "Đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa là 5 sao")
    private Integer rating;

    @Size(max = 200, message = "Bình luận không được vượt quá 200 ký tự")
    private String comment;
}
