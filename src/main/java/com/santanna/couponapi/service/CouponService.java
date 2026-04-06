package com.santanna.couponapi.service;

import com.santanna.couponapi.domain.Coupon;
import com.santanna.couponapi.dto.request.CreateCouponRequest;
import com.santanna.couponapi.dto.response.CouponResponse;
import com.santanna.couponapi.exception.BusinessException;
import com.santanna.couponapi.exception.ResourceNotFoundException;
import com.santanna.couponapi.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final Clock clock;

    public CouponService(CouponRepository couponRepository, Clock clock) {
        this.couponRepository = couponRepository;
        this.clock = clock;
    }

    @Transactional
    public CouponResponse create(CreateCouponRequest request) {
        LocalDate today =  LocalDate.now(clock);
        Coupon coupon = Coupon.create(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published(),
                today
        );

        if (couponRepository.existsByCodeAndDeletedFalse(coupon.getCode())) {
            throw new BusinessException("Coupon code already exists");
        }

        Coupon savedCoupon = couponRepository.save(coupon);
        return CouponResponse.from(savedCoupon);
    }

    @Transactional
    public void delete(UUID id) {
        Coupon coupon = couponRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        coupon.delete();
        couponRepository.save(coupon);
    }
}
