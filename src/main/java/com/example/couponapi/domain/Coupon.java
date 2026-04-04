package com.example.couponapi.domain;

import com.example.couponapi.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "coupons")
public class Coupon {

    private static final BigDecimal MINIMUM_DISCOUNT_VALUE = new BigDecimal("0.5");
    private static final int CODE_LENGTH = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus status;

    @Column(nullable = false)
    private Boolean published;

    @Column(nullable = false)
    private Boolean deleted;

    protected Coupon() {
        // JPA
    }

    private Coupon(
            String code,
            String description,
            BigDecimal discountValue,
            LocalDate expirationDate,
            CouponStatus status,
            Boolean published,
            Boolean deleted
    ) {
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.status = status;
        this.published = published;
        this.deleted = deleted;
    }

    public static Coupon create(
            String rawCode,
            String description,
            BigDecimal discountValue,
            LocalDate expirationDate,
            Boolean published
    ) {
        String sanitizedCode = sanitizeCode(rawCode);
        validateCode(sanitizedCode);
        validateDescription(description);
        validateDiscountValue(discountValue);
        validateExpirationDate(expirationDate);

        return new Coupon(
                sanitizedCode,
                description.trim(),
                discountValue,
                expirationDate,
                CouponStatus.ACTIVE,
                Boolean.TRUE.equals(published),
                false
        );
    }

    public void delete() {
        if (Boolean.TRUE.equals(this.deleted) || CouponStatus.DELETED.equals(this.status)) {
            throw new BusinessException("Coupon already deleted");
        }

        this.deleted = true;
        this.status = CouponStatus.DELETED;
    }

    private static void validateCode(String sanitizedCode) {
        if (sanitizedCode == null || sanitizedCode.length() != CODE_LENGTH) {
            throw new BusinessException("Coupon code must contain exactly 6 alphanumeric characters after sanitization");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessException("Coupon description is required");
        }
    }

    private static void validateDiscountValue(BigDecimal discountValue) {
        if (discountValue == null || discountValue.compareTo(MINIMUM_DISCOUNT_VALUE) < 0) {
            throw new BusinessException("Discount value must be greater than or equal to 0.5");
        }
    }

    private static void validateExpirationDate(LocalDate expirationDate) {
        if (expirationDate == null) {
            throw new BusinessException("Expiration date is required");
        }

        if (expirationDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Expiration date cannot be in the past");
        }
    }

    public static String sanitizeCode(String rawCode) {
        if (rawCode == null) {
            return null;
        }
        return rawCode.replaceAll("[^a-zA-Z0-9]", "");
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public CouponStatus getStatus() {
        return status;
    }

    public Boolean getPublished() {
        return published;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Coupon coupon)) {
            return false;
        }
        return Objects.equals(id, coupon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
