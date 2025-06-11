package com.myproject.brokagefirmchallenge.repo.controller;

import com.myproject.brokagefirmchallenge.repo.manager.OrderManager;
import com.myproject.brokagefirmchallenge.repo.request.CreateOrderRequest;
import com.myproject.brokagefirmchallenge.repo.request.ListOrdersRequest;
import com.myproject.brokagefirmchallenge.repo.response.ApiResponse;
import com.myproject.brokagefirmchallenge.repo.response.PagedResponse;
import com.myproject.brokagefirmchallenge.repo.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Order Management", description = "Order operations")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderManager orderManager;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Create Order", description = "Create a new order for trading")
    public ResponseEntity<ApiResponse<OrderVO>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("REST request to create order: {}", request);
        ApiResponse<OrderVO> response = orderManager.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "List Orders", description = "List orders with filters and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<OrderVO>>> listOrders(
            @Valid @ModelAttribute ListOrdersRequest request) {

        log.info("REST request to list orders: {}", request);
        ApiResponse<PagedResponse<OrderVO>> response = orderManager.listOrders(request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Cancel Order", description = "Cancel a pending order")
    public ResponseEntity<ApiResponse<OrderVO>> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {

        log.info("REST request to cancel order: {}", orderId);
        ApiResponse<OrderVO> response = orderManager.cancelOrder(orderId);

        return ResponseEntity.ok(response);
    }
}