package com.thinkmart.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Size(max = 60) String category,
        @NotBlank @Size(max = 120) String brand,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @DecimalMin("0.0") @DecimalMax("5.0") Double rating,
        @Size(max = 1000) String description,
        @Size(max = 255) String imageUrl,
        @NotNull @Min(0) Integer stock) {
}
