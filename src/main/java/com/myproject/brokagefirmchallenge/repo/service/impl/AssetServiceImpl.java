package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.myproject.brokagefirmchallenge.repo.entity.Asset;
import com.myproject.brokagefirmchallenge.repo.enumtype.AuditAction;
import com.myproject.brokagefirmchallenge.repo.exceptions.AssetNotFoundException;
import com.myproject.brokagefirmchallenge.repo.exceptions.InsufficientBalanceException;
import com.myproject.brokagefirmchallenge.repo.repository.AssetRepository;
import com.myproject.brokagefirmchallenge.repo.service.AssetService;
import com.myproject.brokagefirmchallenge.repo.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AuditService auditService;

    private static final String TRY_ASSET = "TRY";

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void createOrUpdateAsset(Long customerId, String assetName, BigDecimal amount) {
        log.info("Create/Update asset for customer: {}, asset: {}, amount: {}", customerId, assetName, amount);

        Asset asset = assetRepository.findByCustomerIdAndAssetNameForUpdate(customerId, assetName)
                .orElse(null);

        if (asset != null) {
            BigDecimal oldSize = asset.getSize();
            BigDecimal oldUsableSize = asset.getUsableSize();

            asset.setSize(oldSize.add(amount));
            asset.setUsableSize(oldUsableSize.add(amount));

            assetRepository.save(asset);

            auditService.auditBalanceChange(customerId, assetName, oldSize, asset.getSize(),
                    "Asset balance updated");
        } else {
            Asset newAsset = Asset.builder()
                    .customerId(customerId)
                    .assetName(assetName)
                    .size(amount)
                    .usableSize(amount)
                    .averageCost(BigDecimal.ZERO)
                    .build();

            assetRepository.save(newAsset);

            auditService.auditCreate("Asset", newAsset.getId(), newAsset);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName);
    }

    @Override
    @Transactional(readOnly = true)
    public Asset findByCustomerIdAndAssetNameOrThrow(Long customerId, String assetName) {
        return findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new AssetNotFoundException(
                        String.format("Asset not found for customer: %d, asset: %s", customerId, assetName)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> findAssets(Specification<Asset> specification) {
        return assetRepository.findAll(specification);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void blockAsset(Long customerId, String assetName, BigDecimal amount) {
        log.info("Blocking asset for customer: {}, asset: {}, amount: {}",
                customerId, assetName, amount);

        Asset asset = assetRepository
                .findByCustomerIdAndAssetNameForUpdate(customerId, assetName)
                .orElseThrow(() -> new AssetNotFoundException(
                        String.format("Asset not found for customer: %d, asset: %s", customerId, assetName)));

        if (asset.getUsableSize().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Required: %s, Available: %s",
                            amount, asset.getUsableSize()));
        }

        BigDecimal oldUsableSize = asset.getUsableSize();
        asset.setUsableSize(asset.getUsableSize().subtract(amount));
        assetRepository.save(asset);

        auditService.auditAction(AuditAction.ASSET_BLOCKED, "Asset", asset.getId(),
                oldUsableSize.toString(), asset.getUsableSize().toString());
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void releaseAsset(Long customerId, String assetName, BigDecimal amount) {
        log.info("Releasing asset for customer: {}, asset: {}, amount: {}",
                customerId, assetName, amount);

        Asset asset = assetRepository
                .findByCustomerIdAndAssetNameForUpdate(customerId, assetName)
                .orElseThrow(() -> new AssetNotFoundException(
                        String.format("Asset not found for customer: %d, asset: %s", customerId, assetName)));

        BigDecimal oldUsableSize = asset.getUsableSize();
        asset.setUsableSize(asset.getUsableSize().add(amount));

        if (asset.getUsableSize().compareTo(asset.getSize()) > 0) {
            asset.setUsableSize(asset.getSize());
        }

        assetRepository.save(asset);

        auditService.auditAction(AuditAction.ASSET_RELEASED, "Asset", asset.getId(),
                oldUsableSize.toString(), asset.getUsableSize().toString());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void transferAsset(Long fromCustomerId, Long toCustomerId,
                              String assetName, BigDecimal amount) {
        log.info("Transferring asset from customer: {} to customer: {}, asset: {}, amount: {}",
                fromCustomerId, toCustomerId, assetName, amount);

        Asset fromAsset = assetRepository
                .findByCustomerIdAndAssetNameForUpdate(fromCustomerId, assetName)
                .orElseThrow(() -> new AssetNotFoundException(
                        String.format("Asset not found for sender customer: %d", fromCustomerId)));

        if (fromAsset.getUsableSize().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance for transfer. Required: %s, Available: %s",
                            amount, fromAsset.getUsableSize()));
        }

        fromAsset.setSize(fromAsset.getSize().subtract(amount));
        fromAsset.setUsableSize(fromAsset.getUsableSize().subtract(amount));
        assetRepository.save(fromAsset);

        createOrUpdateAsset(toCustomerId, assetName, amount);

        auditService.auditAction(AuditAction.BALANCE_UPDATED, "Asset", fromAsset.getId(),
                "Transfer out: " + amount, "To customer: " + toCustomerId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getUsableBalance(Long customerId, String assetName) {
        return findByCustomerIdAndAssetName(customerId, assetName)
                .map(Asset::getUsableSize)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance(Long customerId, String assetName) {
        return findByCustomerIdAndAssetName(customerId, assetName)
                .map(Asset::getSize)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public void initializeCustomerAssets(Long customerId) {
        log.info("Initializing assets for customer: {}", customerId);

        if (!assetRepository.existsByCustomerIdAndAssetName(customerId, TRY_ASSET)) {
            Asset tryAsset = Asset.builder()
                    .customerId(customerId)
                    .assetName(TRY_ASSET)
                    .size(BigDecimal.ZERO)
                    .usableSize(BigDecimal.ZERO)
                    .averageCost(BigDecimal.ONE)
                    .build();

            assetRepository.save(tryAsset);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnoughBalance(Long customerId, String assetName, BigDecimal requiredAmount) {
        return getUsableBalance(customerId, assetName).compareTo(requiredAmount) >= 0;
    }
}
