package com.santanna.couponapi.repository;

import com.santanna.couponapi.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    boolean existsByCodeAndDeletedFalse(String code);
}
