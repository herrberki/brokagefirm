package com.myproject.brokagefirmchallenge.repo.service;

import com.myproject.brokagefirmchallenge.repo.entity.Asset;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AssetService {

    void createOrUpdateAsset(Long customerId, String assetName, BigDecimal amount);

    Optional<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName);

    Asset findByCustomerIdAndAssetNameOrThrow(Long customerId, String assetName);

    List<Asset> findAssets(Specification<Asset> specification);

    void blockAsset(Long customerId, String assetName, BigDecimal amount);

    void releaseAsset(Long customerId, String assetName, BigDecimal amount);

    void transferAsset(Long fromCustomerId, Long toCustomerId, String assetName, BigDecimal amount);

    BigDecimal getUsableBalance(Long customerId, String assetName);

    BigDecimal getTotalBalance(Long customerId, String assetName);

    boolean isEnoughBalance(Long customerId, String assetName, BigDecimal requiredAmount);

    void initializeCustomerAssets(Long customerId);
}
