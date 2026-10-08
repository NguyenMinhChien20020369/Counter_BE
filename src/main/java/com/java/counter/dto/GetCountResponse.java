package com.java.counter.dto;

import com.java.counter.entity.DateCount;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record GetCountResponse(
        Integer total,

        List<DateCount> dateCountList
) {
}
