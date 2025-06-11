package com.myproject.brokagefirmchallenge.repo.service;

import com.myproject.brokagefirmchallenge.repo.entity.Customer;

import java.util.Optional;

import com.myproject.brokagefirmchallenge.repo.enumtype.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    Customer createCustomer(Customer customer);

    Customer updateCustomer(Long id, Customer customer);

    Optional<Customer> findById(Long id);

    void unlockAccount(Long customerId);

    Page<Customer> findAll(Pageable pageable);

    Customer changePassword(Long customerId, String oldPassword, String newPassword);

    Customer updateStatus(Long customerId, CustomerStatus status);

    void incrementFailedLoginAttempts(String username);

    void resetFailedLoginAttempts(String username);

    void lockAccount(String username);

    boolean isAccountLocked(Long customerId);

    void updateLastLoginDate(Long customerId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}