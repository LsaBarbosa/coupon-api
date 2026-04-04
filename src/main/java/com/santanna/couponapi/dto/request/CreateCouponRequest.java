package com.santanna.couponapi.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "CreateCouponRequest", description = "Request payload for creating a coupon")
public record CreateCouponRequest(
        @NotBlank
        @Schema(example = "AB-12@C")
        String code,

        @NotBlank
        @Schema(example = "Black Friday discount")
        String description,

        @NotNull
        @DecimalMin(value = "0.5", inclusive = true)
        @Schema(example = "10.50")
        BigDecimal discountValue,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "2026-12-31")
        LocalDate expirationDate,

        @Schema(example = "false", defaultValue = "false")
        Boolean published
) {
}
