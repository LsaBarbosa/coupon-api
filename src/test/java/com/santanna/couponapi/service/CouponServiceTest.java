package com.santanna.couponapi.service;

import com.santanna.couponapi.domain.Coupon;
import com.santanna.couponapi.domain.CouponStatus;
import com.santanna.couponapi.dto.request.CreateCouponRequest;
import com.santanna.couponapi.dto.response.CouponResponse;
import com.santanna.couponapi.exception.BusinessException;
import com.santanna.couponapi.exception.ResourceNotFoundException;
import com.santanna.couponapi.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void shouldSaveCouponWhenRequestIsValid() {
        CreateCouponRequest request = new CreateCouponRequest(
                "AB-12@CD",
                "Summer campaign",
                new BigDecimal("10.00"),
                LocalDate.now().plusDays(10),
                true
        );

        Coupon savedCoupon = Coupon.create(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published()
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
                LocalDate.now().plusDays(10),
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
                LocalDate.now().plusDays(10),
                true
        );

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(coupon)).thenReturn(coupon);

        couponService.delete(couponId);

        assertEquals(CouponStatus.DELETED, coupon.getStatus());
        verify(couponRepository).save(coupon);
    }

    @Test
    void shouldFailToDeleteWhenCouponDoesNotExist() {
        UUID couponId = UUID.randomUUID();
        when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> couponService.delete(couponId));

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
                LocalDate.now().plusDays(10),
                true
        );
        coupon.delete();

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));

        BusinessException exception = assertThrows(BusinessException.class, () -> couponService.delete(couponId));

        assertEquals("Coupon already deleted", exception.getMessage());
        verify(couponRepository, never()).save(any(Coupon.class));
    }
}
