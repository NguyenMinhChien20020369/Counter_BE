package com.java.counter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Date;

public record GetCountDTO(
        @NotBlank(message = "Vui lòng nhập tháng")
        Integer month,

        @NotBlank(message = "Vui lòng nhập năm")
        Integer year
) {
}
