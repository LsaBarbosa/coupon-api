package com.example.couponapi.dto.response;

import com.example.couponapi.domain.Coupon;
import com.example.couponapi.domain.CouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "CouponResponse", description = "Response payload for coupon operations")
public record CouponResponse(
        UUID id,
        String code,
        String description,
        BigDecimal discountValue,
        LocalDate expirationDate,
        CouponStatus status,
        Boolean published
) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountValue(),
                coupon.getExpirationDate(),
                coupon.getStatus(),
                coupon.getPublished()
        );
    }
}
