package com.homestay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AboutStatItem {

    @NotBlank
    private String value;

    @NotBlank
    private String label;
}
