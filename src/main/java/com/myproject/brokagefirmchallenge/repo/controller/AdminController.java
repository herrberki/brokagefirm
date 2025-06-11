package com.myproject.brokagefirmchallenge.repo.controller;

import com.myproject.brokagefirmchallenge.repo.manager.AdminManager;
import com.myproject.brokagefirmchallenge.repo.request.MatchOrdersRequest;
import com.myproject.brokagefirmchallenge.repo.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Admin Operations", description = "Admin only operations")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminManager adminManager;

    @PostMapping("/orders/match")
    @Operation(summary = "Match Orders", description = "Manually trigger order matching for an asset")
    public ResponseEntity<ApiResponse<String>> matchOrders(
            @Valid @RequestBody MatchOrdersRequest request) {

        log.info("REST request to match orders for asset: {}", request.getAssetName());
        ApiResponse<String> response = adminManager.matchOrders(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders/match-all")
    @Operation(summary = "Match All Orders", description = "Manually trigger order matching for all assets")
    public ResponseEntity<ApiResponse<String>> matchAllOrders() {

        log.info("REST request to match all pending orders");
        ApiResponse<String> response = adminManager.matchAllOrders();

        return ResponseEntity.ok(response);
    }
}
