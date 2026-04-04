package com.example.couponapi.repository;

import com.example.couponapi.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    boolean existsByCodeAndDeletedFalse(String code);
}
