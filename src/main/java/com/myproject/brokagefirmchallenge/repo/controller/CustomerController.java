package com.myproject.brokagefirmchallenge.repo.controller;

import com.myproject.brokagefirmchallenge.repo.enumtype.CustomerStatus;
import com.myproject.brokagefirmchallenge.repo.manager.CustomerManager;
import com.myproject.brokagefirmchallenge.repo.request.ChangePasswordRequest;
import com.myproject.brokagefirmchallenge.repo.request.CreateCustomerRequest;
import com.myproject.brokagefirmchallenge.repo.response.ApiResponse;
import com.myproject.brokagefirmchallenge.repo.vo.CustomerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Customer Management", description = "Customer operations")
public class CustomerController {

    private final CustomerManager customerManager;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Customer", description = "Create a new customer (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CustomerVO>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {

        log.info("REST request to create customer: {}", request.getUsername());
        ApiResponse<CustomerVO> response = customerManager.createCustomer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    @Operation(summary = "Get Customer", description = "Get customer details")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CustomerVO>> getCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {

        log.info("REST request to get customer: {}", customerId);
        ApiResponse<CustomerVO> response = customerManager.getCustomer(customerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List Customers", description = "List all customers (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Page<CustomerVO>>> listCustomers(
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("REST request to list customers");
        ApiResponse<Page<CustomerVO>> response = customerManager.listCustomers(pageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    @Operation(summary = "Update Customer", description = "Update customer details")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CustomerVO>> updateCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Valid @RequestBody CreateCustomerRequest request) {

        log.info("REST request to update customer: {}", customerId);
        ApiResponse<CustomerVO> response = customerManager.updateCustomer(customerId, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Change Password", description = "Change current user's password")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CustomerVO>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("REST request to change password");
        ApiResponse<CustomerVO> response = customerManager.changePassword(request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{customerId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Customer Status", description = "Update customer status (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CustomerVO>> updateCustomerStatus(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "New Status") @RequestParam CustomerStatus status) {

        log.info("REST request to update customer {} status to {}", customerId, status);
        ApiResponse<CustomerVO> response = customerManager.updateCustomerStatus(customerId, status);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/customers/{customerId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock Customer Account", description = "Unlock a locked customer account (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> unlockCustomerAccount(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {

        log.info("REST request to unlock customer account: {}", customerId);
        ApiResponse<Void> response = customerManager.unlockCustomerAccount(customerId);
        return ResponseEntity.ok(response);
    }

}
