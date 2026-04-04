package com.example.couponapi.service;

import com.example.couponapi.domain.Coupon;
import com.example.couponapi.dto.request.CreateCouponRequest;
import com.example.couponapi.dto.response.CouponResponse;
import com.example.couponapi.exception.BusinessException;
import com.example.couponapi.exception.ResourceNotFoundException;
import com.example.couponapi.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    public CouponResponse create(CreateCouponRequest request) {
        Coupon coupon = Coupon.create(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published()
        );

        if (couponRepository.existsByCodeAndDeletedFalse(coupon.getCode())) {
            throw new BusinessException("Coupon code already exists");
        }

        Coupon savedCoupon = couponRepository.save(coupon);
        return CouponResponse.from(savedCoupon);
    }

    @Transactional
    public void delete(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        coupon.delete();
        couponRepository.save(coupon);
    }
}
