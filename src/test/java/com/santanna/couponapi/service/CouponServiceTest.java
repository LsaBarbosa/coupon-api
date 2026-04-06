package com.santanna.couponapi.service;

import com.santanna.couponapi.domain.Coupon;
import com.santanna.couponapi.domain.CouponStatus;
import com.santanna.couponapi.dto.request.CreateCouponRequest;
import com.santanna.couponapi.dto.response.CouponResponse;
import com.santanna.couponapi.exception.BusinessException;
import com.santanna.couponapi.exception.ResourceNotFoundException;
import com.santanna.couponapi.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    private CouponService couponService;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(
                Instant.parse("2026-04-05T12:00:00Z"),
                ZoneId.of("America/Sao_Paulo")
        );
        couponService = new CouponService(couponRepository, fixedClock);
    }

    @Test
    void shouldSaveCouponWhenRequestIsValid() {
        CreateCouponRequest request = new CreateCouponRequest(
                "AB-12@CD",
                "Summer campaign",
                new BigDecimal("10.00"),
                LocalDate.of(2026, 4, 15),
                true
        );

        Coupon savedCoupon = Coupon.create(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published(),
                LocalDate.now(fixedClock)
        );

        when(couponRepository.existsByCodeAndDeletedFalse("AB12CD")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        CouponResponse response = couponService.create(request);

        assertEquals("AB12CD", response.code());
        assertEquals(CouponStatus.ACTIVE, response.status());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void shouldFailWhenCouponCodeAlreadyExists() {
        CreateCouponRequest request = new CreateCouponRequest(
                "AB-12@CD",
                "Summer campaign",
                new BigDecimal("10.00"),
                LocalDate.of(2026, 4, 15),
                true
        );

        when(couponRepository.existsByCodeAndDeletedFalse("AB12CD")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.create(request));

        assertEquals("Coupon code already exists", exception.getMessage());
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void shouldDeleteCouponWhenItExists() {
        UUID couponId = UUID.randomUUID();
        Coupon coupon = Coupon.create(
                "ABC123",
                "Summer campaign",
                new BigDecimal("10.00"),
                LocalDate.of(2026, 4, 15),
                true,
                LocalDate.now(fixedClock)
        );

        when(couponRepository.findByIdAndDeletedFalse(couponId)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(coupon)).thenReturn(coupon);

        couponService.delete(couponId);

        assertEquals(CouponStatus.DELETED, coupon.getStatus());
        verify(couponRepository).save(coupon);
    }

    @Test
    void shouldFailToDeleteWhenCouponDoesNotExist() {
        UUID couponId = UUID.randomUUID();
        when(couponRepository.findByIdAndDeletedFalse(couponId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> couponService.delete(couponId)
        );

        assertEquals("Coupon not found", exception.getMessage());
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void shouldFailToDeleteAlreadyDeletedCoupon() {
        UUID couponId = UUID.randomUUID();
        Coupon coupon = Coupon.create(
                "ABC123",
                "Summer campaign",
                new BigDecimal("10.00"),
                LocalDate.of(2026, 4, 15),
                true,
                LocalDate.now(fixedClock)
        );
        coupon.delete();

        when(couponRepository.findByIdAndDeletedFalse(couponId)).thenReturn(Optional.of(coupon));

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.delete(couponId));

        assertEquals("Coupon already deleted", exception.getMessage());
        verify(couponRepository, never()).save(any(Coupon.class));
    }
}