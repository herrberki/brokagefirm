package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.myproject.brokagefirmchallenge.repo.entity.Customer;
import com.myproject.brokagefirmchallenge.repo.enumtype.AuditAction;
import com.myproject.brokagefirmchallenge.repo.enumtype.CustomerStatus;
import com.myproject.brokagefirmchallenge.repo.exceptions.CustomerNotFoundException;
import com.myproject.brokagefirmchallenge.repo.exceptions.DuplicateEmailException;
import com.myproject.brokagefirmchallenge.repo.exceptions.DuplicateUsernameException;
import com.myproject.brokagefirmchallenge.repo.exceptions.InvalidPasswordException;
import com.myproject.brokagefirmchallenge.repo.repository.CustomerRepository;
import com.myproject.brokagefirmchallenge.repo.service.AuditService;
import com.myproject.brokagefirmchallenge.repo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    private static final int MAX_FAILED_ATTEMPTS = 3;

    @Override
    public Customer createCustomer(Customer customer) {
        log.info("Creating new customer with username: {}", customer.getUsername());

        if (customerRepository.existsByUsername(customer.getUsername())) {
            throw new DuplicateUsernameException("Username already exists: " + customer.getUsername());
        }

        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + customer.getEmail());
        }

        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setFailedLoginAttempts(0);
        customer.setIsLocked(false);

        Customer savedCustomer = customerRepository.save(customer);

        auditService.auditCreate("Customer", savedCustomer.getId(), savedCustomer);

        log.info("Customer created successfully with ID: {}", savedCustomer.getId());
        return savedCustomer;
    }

    @Override
    public Customer updateCustomer(Long id, Customer customer) {
        log.info("Updating customer with ID: {}", id);

        Customer existingCustomer = getCustomerOrThrow(id);
        Customer oldCustomer = Customer.builder()
                .username(existingCustomer.getUsername())
                .email(existingCustomer.getEmail())
                .fullName(existingCustomer.getFullName())
                .phoneNumber(existingCustomer.getPhoneNumber())
                .build();

        Optional.ofNullable(customer.getEmail())
                .filter(email -> !email.equals(existingCustomer.getEmail()))
                .ifPresent(email -> {
                    validateUniqueEmail(email);
                    existingCustomer.setEmail(email);
                });

        Optional.ofNullable(customer.getFullName())
                .ifPresent(existingCustomer::setFullName);

        Optional.ofNullable(customer.getPhoneNumber())
                .ifPresent(existingCustomer::setPhoneNumber);

        Customer updatedCustomer = customerRepository.save(existingCustomer);

        auditService.auditUpdate("Customer", updatedCustomer.getId(), oldCustomer, updatedCustomer);

        return updatedCustomer;
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Override
    public Customer changePassword(Long customerId, String oldPassword, String newPassword) {
        log.info("Changing password for customer ID: {}", customerId);

        Customer customer = findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect");
        }

        customer.setPassword(passwordEncoder.encode(newPassword));
        Customer updatedCustomer = customerRepository.save(customer);

        auditService.auditAction(AuditAction.PASSWORD_CHANGED, "Customer", customerId, null, null);

        return updatedCustomer;
    }

    @Override
    public Customer updateStatus(Long customerId, CustomerStatus status) {
        log.info("Updating status for customer ID: {} to {}", customerId, status);

        Customer customer = findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        CustomerStatus oldStatus = customer.getStatus();
        customer.setStatus(status);

        Customer updatedCustomer = customerRepository.save(customer);

        auditService.auditAction(AuditAction.UPDATE, "Customer", customerId,
                oldStatus.name(), status.name());

        return updatedCustomer;
    }

    @Override
    public void incrementFailedLoginAttempts(String username) {
        customerRepository.findByUsername(username).ifPresent(customer -> {
            int attempts = customer.getFailedLoginAttempts() + 1;
            customerRepository.updateFailedLoginAttempts(customer.getId(), attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                lockAccount(username);
            }
        });
    }

    @Override
    public void unlockAccount(Long customerId) {
        Customer customer = getCustomerOrThrow(customerId);

        customer.setIsLocked(false);
        customer.setFailedLoginAttempts(0);
        customer.setStatus(CustomerStatus.ACTIVE);

        customerRepository.save(customer);

        auditService.auditAction(AuditAction.ACCOUNT_UNLOCKED, "Customer", customerId, null, "Account unlocked by admin");
    }



    @Override
    public void resetFailedLoginAttempts(String username) {
        customerRepository.findByUsername(username).ifPresent(customer -> {
            customerRepository.updateFailedLoginAttempts(customer.getId(), 0);
        });
    }

    @Override
    public void lockAccount(String username) {
        log.warn("Locking account for username: {}", username);

        customerRepository.findByUsername(username).ifPresent(customer -> {
            customerRepository.updateAccountLockStatus(customer.getId(), true);
            customer.setStatus(CustomerStatus.BLOCKED);
            customerRepository.save(customer);

            auditService.auditAction(AuditAction.ACCOUNT_LOCKED, "Customer",
                    customer.getId(), null, "Max failed login attempts reached");
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAccountLocked(Long customerId) {
        return customerRepository.findById(customerId)
                .map(Customer::getIsLocked)
                .orElse(false);
    }

    @Override
    public void updateLastLoginDate(Long customerId) {
        customerRepository.updateLastLoginDate(customerId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return customerRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    private Customer getCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
    }

    private void validateUniqueEmail(String email) {
        if (customerRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already exists: " + email);
        }
    }
}
