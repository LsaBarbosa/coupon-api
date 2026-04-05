package com.santanna.couponapi.domain;

import com.santanna.couponapi.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CouponTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 4, 5);

    @Test
    void shouldCreateValidCoupon() {
        Coupon coupon = Coupon.create(
                "ABC123",
                "Summer campaign",
                new BigDecimal("10.00"),
                TODAY.plusDays(10),
                true,
                TODAY
        );

        assertEquals("ABC123", coupon.getCode());
        assertEquals(CouponStatus.ACTIVE, coupon.getStatus());
        assertTrue(coupon.getPublished());
        assertFalse(coupon.getDeleted());
    }

    @Test
    void shouldSanitizeCodeByRemovingSpecialCharacters() {
        assertEquals("AB12CD", Coupon.sanitizeCode("AB-12@CD"));
    }

    @Test
    void shouldAcceptSanitizedCodeWithExactlySixCharacters() {
        Coupon coupon = Coupon.create(
                "AB-12@CD",
                "Summer campaign",
                new BigDecimal("10.00"),
                TODAY.plusDays(10),
                false,
                TODAY
        );

        assertEquals("AB12CD", coupon.getCode());
    }

    @Test
    void shouldRejectDiscountValueBelowMinimum() {
        BusinessException exception = assertThrows(BusinessException.class, () -> Coupon.create(
                "ABC123",
                "Summer campaign",
                new BigDecimal("0.49"),
                TODAY.plusDays(10),
                false,
                TODAY
        ));

        assertEquals("Discount value must be greater than or equal to 0.5", exception.getMessage());
    }

    @Test
    void shouldRejectPastExpirationDate() {
        BusinessException exception = assertThrows(BusinessException.class, () -> Coupon.create(
                "ABC123",
                "Summer campaign",
                new BigDecimal("10.00"),
                TODAY.minusDays(1),
                false,
                TODAY
        ));

        assertEquals("Expiration date cannot be in the past", exception.getMessage());
    }

    @Test
    void shouldRejectCodeWhenSanitizedLengthIsDifferentFromSix() {
        BusinessException exception = assertThrows(BusinessException.class, () -> Coupon.create(
                "A-1",
                "Summer campaign",
                new BigDecimal("10.00"),
                TODAY.plusDays(10),
                false,
                TODAY
        ));

        assertEquals("Coupon code must contain exactly 6 alphanumeric characters after sanitization", exception.getMessage());
    }

    @Test
    void shouldSoftDeleteCoupon() {
        Coupon coupon = Coupon.create(
                "ABC123",
                "Summer campaign",
                new BigDecimal("10.00"),
                TODAY.plusDays(10),
                true,
                TODAY
        );

        coupon.delete();

        assertTrue(coupon.getDeleted());
        assertEquals(CouponStatus.DELETED, coupon.getStatus());
    }

    @Test
    void shouldNotDeleteCouponTwice() {
        Coupon coupon = Coupon.create(
                "ABC123",
                "Summer campaign",
                new BigDecimal("10.00"),
                TODAY.plusDays(10),
                true,
                TODAY
        );
        coupon.delete();

        BusinessException exception = assertThrows(BusinessException.class, coupon::delete);

        assertEquals("Coupon already deleted", exception.getMessage());
    }
}
