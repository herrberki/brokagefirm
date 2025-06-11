package com.myproject.brokagefirmchallenge.repo.manager;

import com.myproject.brokagefirmchallenge.repo.request.MatchOrdersRequest;
import com.myproject.brokagefirmchallenge.repo.response.ApiResponse;
import com.myproject.brokagefirmchallenge.repo.service.OrderMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminManager {

    private final OrderMatchingService orderMatchingService;

    @Transactional
    public ApiResponse<String> matchOrders(MatchOrdersRequest request) {
        log.info("Admin triggered order matching for asset: {}", request.getAssetName());

        try {
            orderMatchingService.matchOrders(request.getAssetName());

            return ApiResponse.success(
                    "Order matching completed for asset: " + request.getAssetName(),
                    "Order matching completed successfully"
            );
        } catch (Exception e) {
            log.error("Error during order matching", e);
            return ApiResponse.error(
                    "Order matching failed: " + e.getMessage(),
                    "ORDER_MATCHING_FAILED"
            );
        }
    }

    @Transactional
    public ApiResponse<String> matchAllOrders() {
        log.info("Admin triggered order matching for all assets");

        try {
            orderMatchingService.matchAllPendingOrders();

            return ApiResponse.success(
                    "Order matching completed for all assets",
                    "Order matching completed successfully"
            );
        } catch (Exception e) {
            log.error("Error during order matching", e);
            return ApiResponse.error(
                    "Order matching failed: " + e.getMessage(),
                    "ORDER_MATCHING_FAILED"
            );
        }
    }
}
