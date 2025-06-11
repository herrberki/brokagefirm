package com.myproject.brokagefirmchallenge.repo.manager;

import com.myproject.brokagefirmchallenge.repo.converter.CustomerMapper;
import com.myproject.brokagefirmchallenge.repo.entity.Customer;
import com.myproject.brokagefirmchallenge.repo.enumtype.CustomerStatus;
import com.myproject.brokagefirmchallenge.repo.exceptions.CustomerNotFoundException;
import com.myproject.brokagefirmchallenge.repo.exceptions.InvalidPasswordException;
import com.myproject.brokagefirmchallenge.repo.exceptions.UnauthorizedAccessException;
import com.myproject.brokagefirmchallenge.repo.exceptions.ValidationException;
import com.myproject.brokagefirmchallenge.repo.request.ChangePasswordRequest;
import com.myproject.brokagefirmchallenge.repo.request.CreateCustomerRequest;
import com.myproject.brokagefirmchallenge.repo.response.ApiResponse;
import com.myproject.brokagefirmchallenge.repo.security.SecurityContextManager;
import com.myproject.brokagefirmchallenge.repo.service.AssetService;
import com.myproject.brokagefirmchallenge.repo.service.CustomerService;
import com.myproject.brokagefirmchallenge.repo.validator.CustomerValidator;
import com.myproject.brokagefirmchallenge.repo.vo.CustomerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerManager {

    private final CustomerMapper customerMapper;
    private final CustomerService customerService;
    private final AssetService assetService;
    private final SecurityContextManager securityContextManager;
    private final CustomerValidator customerValidator;

    @Transactional
    public ApiResponse<CustomerVO> createCustomer(CreateCustomerRequest request) {
        log.info("Creating new customer with username: {}", request.getUsername());

        customerValidator.validate(request);

        Customer customer = customerMapper.toEntity(request);
        Customer createdCustomer = customerService.createCustomer(customer);

        assetService.initializeCustomerAssets(createdCustomer.getId());

        CustomerVO customerVO = customerMapper.toVO(createdCustomer);

        log.info("Customer created successfully with ID: {}", createdCustomer.getId());
        return ApiResponse.success(customerVO, "Customer created successfully");
    }

    @Transactional
    public ApiResponse<CustomerVO> updateCustomer(Long customerId, CreateCustomerRequest request) {
        log.info("Updating customer ID: {}", customerId);

        if (!securityContextManager.isAdmin() &&
                !securityContextManager.getCurrentCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Unauthorized access to customer");
        }

        Customer existingCustomer = customerService.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        customerMapper.updateEntityFromDto(request, existingCustomer);
        Customer updatedCustomer = customerService.updateCustomer(customerId, existingCustomer);

        CustomerVO customerVO = customerMapper.toVO(updatedCustomer);

        return ApiResponse.success(customerVO, "Customer updated successfully");
    }

    @Transactional
    public ApiResponse<CustomerVO> changePassword(ChangePasswordRequest request) {
        log.info("Changing password for current user");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidPasswordException("New password and confirmation do not match");
        }

        Long customerId = securityContextManager.getCurrentCustomerId();

        Customer customer = customerService.changePassword(
                customerId, request.getOldPassword(), request.getNewPassword());

        CustomerVO customerVO = customerMapper.toVO(customer);

        return ApiResponse.success(customerVO, "Password changed successfully");
    }

    @Transactional(readOnly = true)
    public ApiResponse<CustomerVO> getCustomer(Long customerId) {
        log.debug("Getting customer ID: {}", customerId);

        if (!securityContextManager.isAdmin() &&
                !securityContextManager.getCurrentCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Unauthorized access to customer");
        }

        Customer customer = customerService.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        CustomerVO customerVO = customerMapper.toVO(customer);

        return ApiResponse.success(customerVO);
    }

    @Transactional(readOnly = true)
    public ApiResponse<Page<CustomerVO>> listCustomers(Pageable pageable) {
        log.debug("Listing customers with pageable: {}", pageable);

        if (!securityContextManager.isAdmin()) {
            throw new UnauthorizedAccessException("Only admin can list all customers");
        }

        Page<Customer> customerPage = customerService.findAll(pageable);
        Page<CustomerVO> voPage = customerMapper.toVOPage(customerPage);

        return ApiResponse.success(voPage);
    }

    @Transactional
    public ApiResponse<CustomerVO> updateCustomerStatus(Long customerId, CustomerStatus status) {
        log.info("Updating customer: {} status to: {}", customerId, status);

        if (!securityContextManager.isAdmin()) {
            throw new UnauthorizedAccessException("Only admin can update customer status");
        }

        Customer customer = customerService.updateStatus(customerId, status);
        CustomerVO customerVO = customerMapper.toVO(customer);

        return ApiResponse.success(customerVO, "Customer status updated successfully");
    }

    @Transactional
    public ApiResponse<Void> unlockCustomerAccount(Long customerId) {
        log.info("Admin requested to unlock customer account: {}", customerId);

        if (!securityContextManager.isAdmin()) {
            throw new UnauthorizedAccessException("Only admin can unlock customer accounts");
        }

        if (!customerService.isAccountLocked(customerId)) {
            throw new ValidationException("Account is not locked.");
        }

        customerService.unlockAccount(customerId);

        log.info("Customer account unlocked: {}", customerId);
        return ApiResponse.success(null, "Account unlocked successfully");
    }



}