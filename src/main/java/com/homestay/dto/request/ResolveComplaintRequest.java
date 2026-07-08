package com.homestay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** SCR-54: body cho Admin resolve complaint. */
@Data
public class ResolveComplaintRequest {

    @NotBlank(message = "resolution bat buoc")
    private String resolution;
}