package com.homestay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AboutValueItem {

    @NotBlank
    private String num;

    @NotBlank
    private String title;

    @NotBlank
    private String desc;
}
