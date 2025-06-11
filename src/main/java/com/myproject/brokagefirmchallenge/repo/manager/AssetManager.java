package com.myproject.brokagefirmchallenge.repo.manager;

import com.myproject.brokagefirmchallenge.repo.converter.AssetMapper;
import com.myproject.brokagefirmchallenge.repo.entity.Asset;
import com.myproject.brokagefirmchallenge.repo.exceptions.UnauthorizedAccessException;
import com.myproject.brokagefirmchallenge.repo.request.ListAssetsRequest;
import com.myproject.brokagefirmchallenge.repo.response.ApiResponse;
import com.myproject.brokagefirmchallenge.repo.security.SecurityContextManager;
import com.myproject.brokagefirmchallenge.repo.service.AssetService;
import com.myproject.brokagefirmchallenge.repo.specifications.AssetSpecifications;
import com.myproject.brokagefirmchallenge.repo.validator.AssetValidator;
import com.myproject.brokagefirmchallenge.repo.vo.AssetVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetManager {

    private final AssetMapper assetMapper;
    private final AssetService assetService;
    private final SecurityContextManager securityContextManager;
    private final AssetValidator assetValidator;

    @Transactional(readOnly = true)
    public ApiResponse<List<AssetVO>> listAssets(ListAssetsRequest request) {
        log.debug("Listing assets with criteria: {}", request);

        if (!securityContextManager.isAdmin()) {
            request.setCustomerId(securityContextManager.getCurrentCustomerId());
        }

        assetValidator.validate(request);

        Specification<Asset> spec = createAssetSpecification(request);

        List<Asset> assets = assetService.findAssets(spec);

        if (!request.isIncludeZeroBalance()) {
            assets = assets.stream()
                    .filter(a -> a.getSize().compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());
        }

        List<AssetVO> assetVOs = assetMapper.toVOList(assets);

        assetVOs.sort(Comparator.comparing(AssetVO::getCurrentMarketValue,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return ApiResponse.success(assetVOs);
    }

    @Transactional(readOnly = true)
    public ApiResponse<AssetVO> getAsset(Long customerId, String assetName) {
        log.debug("Getting asset for customer: {}, asset: {}", customerId, assetName);

        if (!securityContextManager.isAdmin() &&
                !securityContextManager.getCurrentCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Unauthorized access to asset");
        }

        Asset asset = assetService.findByCustomerIdAndAssetNameOrThrow(customerId, assetName);
        AssetVO assetVO = assetMapper.toVO(asset);

        return ApiResponse.success(assetVO);
    }

    @Transactional
    public ApiResponse<AssetVO> transferAsset(Long toCustomerId, String assetName, BigDecimal amount) {
        log.info("Transferring {} {} to customer {}", amount, assetName, toCustomerId);

        Long fromCustomerId = securityContextManager.getCurrentCustomerId();

        assetValidator.validateTransfer(fromCustomerId, toCustomerId, assetName, amount);

        assetService.transferAsset(fromCustomerId, toCustomerId, assetName, amount);

        Asset updatedAsset = assetService.findByCustomerIdAndAssetNameOrThrow(fromCustomerId, assetName);
        AssetVO assetVO = assetMapper.toVO(updatedAsset);

        log.info("Transfer completed successfully");
        return ApiResponse.success(assetVO, "Asset transferred successfully");
    }

    private Specification<Asset> createAssetSpecification(ListAssetsRequest request) {
        Specification<Asset> spec = AssetSpecifications.hasCustomerId(request.getCustomerId());

        spec = spec.and(AssetSpecifications.hasAssetName(request.getAssetName()));
        spec = spec.and(AssetSpecifications.hasSizeGreaterThan(request.getMinSize()));
        spec = spec.and(AssetSpecifications.hasUsableSizeGreaterThan(request.getMinUsableSize()));

        return spec;
    }

}
