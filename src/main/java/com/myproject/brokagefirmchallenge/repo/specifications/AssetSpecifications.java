package com.myproject.brokagefirmchallenge.repo.specifications;
import com.myproject.brokagefirmchallenge.repo.entity.Asset;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class AssetSpecifications {

    public static Specification<Asset> hasCustomerId(Long customerId) {
        return (root, query, criteriaBuilder) -> {
            if (customerId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("customerId"), customerId);
        };
    }

    public static Specification<Asset> hasAssetName(String assetName) {
        return (root, query, criteriaBuilder) -> {
            if (assetName == null || assetName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("assetName"), assetName);
        };
    }

    public static Specification<Asset> hasSizeGreaterThan(BigDecimal minSize) {
        return (root, query, criteriaBuilder) -> {
            if (minSize == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("size"), minSize);
        };
    }

    public static Specification<Asset> hasUsableSizeGreaterThan(BigDecimal minUsableSize) {
        return (root, query, criteriaBuilder) -> {
            if (minUsableSize == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("usableSize"), minUsableSize);
        };
    }

}
