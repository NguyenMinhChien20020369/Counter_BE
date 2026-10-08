package com.java.counter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Date;

public record CountDTO(
        @NotBlank(message = "Vui lòng nhập ngày")
        Date date,

        @NotBlank(message = "Vui lòng nhập số lượng")
        @Pattern(regexp = "/^-?1$/\n", message = "Giá trị tăng giảm chỉ được phép là 1 hoặc -1")
        Integer count
) {
}
