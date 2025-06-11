package com.myproject.brokagefirmchallenge.repo.controller;

import com.myproject.brokagefirmchallenge.repo.manager.AssetManager;
import com.myproject.brokagefirmchallenge.repo.request.ListAssetsRequest;
import com.myproject.brokagefirmchallenge.repo.response.ApiResponse;
import com.myproject.brokagefirmchallenge.repo.vo.AssetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Asset Management", description = "Asset and balance operations")
@SecurityRequirement(name = "Bearer Authentication")
public class AssetController {

    private final AssetManager assetManager;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "List Assets", description = "List customer assets with filters")
    public ResponseEntity<ApiResponse<List<AssetVO>>> listAssets(
            @Valid @ModelAttribute ListAssetsRequest request) {

        log.info("REST request to list assets: {}", request);
        ApiResponse<List<AssetVO>> response = assetManager.listAssets(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}/{assetName}")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    @Operation(summary = "Get Asset", description = "Get specific asset details")
    public ResponseEntity<ApiResponse<AssetVO>> getAsset(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Asset Name") @PathVariable String assetName) {

        log.info("REST request to get asset for customer: {} asset: {}", customerId, assetName);
        ApiResponse<AssetVO> response = assetManager.getAsset(customerId, assetName);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Transfer Asset", description = "Transfer asset to another customer")
    public ResponseEntity<ApiResponse<AssetVO>> transferAsset(
            @Parameter(description = "Recipient Customer ID") @RequestParam Long toCustomerId,
            @Parameter(description = "Asset Name") @RequestParam String assetName,
            @Parameter(description = "Amount to Transfer") @RequestParam BigDecimal amount) {

        log.info("REST request to transfer {} {} to customer {}", amount, assetName, toCustomerId);
        ApiResponse<AssetVO> response = assetManager.transferAsset(toCustomerId, assetName, amount);

        return ResponseEntity.ok(response);
    }
}
