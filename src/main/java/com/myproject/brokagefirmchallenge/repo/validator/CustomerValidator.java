package com.myproject.brokagefirmchallenge.repo.validator;

import com.myproject.brokagefirmchallenge.repo.exceptions.DuplicateEmailException;
import com.myproject.brokagefirmchallenge.repo.exceptions.DuplicateUsernameException;
import com.myproject.brokagefirmchallenge.repo.exceptions.ValidationException;
import com.myproject.brokagefirmchallenge.repo.request.CreateCustomerRequest;
import com.myproject.brokagefirmchallenge.repo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerValidator extends BaseValidator<CreateCustomerRequest> {

    private final CustomerService customerService;

    @Override
    public void validate(CreateCustomerRequest request) {
        validateBasicFields(request);
        validateUsername(request.getUsername());
        validateEmail(request.getEmail());
        validatePassword(request.getPassword());
        validatePhoneNumber(request.getPhoneNumber());
    }

    private void validateBasicFields(CreateCustomerRequest request) {
        validateNotEmpty(request.getUsername(), "Username");
        validateNotEmpty(request.getPassword(), "Password");
        validateNotEmpty(request.getEmail(), "Email");
        validateNotEmpty(request.getFullName(), "Full name");
    }

    private void validateUsername(String username) {
        if (username.length() < 3 || username.length() > 50) {
            throw new ValidationException("Username must be between 3 and 50 characters");
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new ValidationException("Username can only contain letters, numbers and underscore");
        }

        if (customerService.existsByUsername(username)) {
            throw new DuplicateUsernameException("Username already exists: " + username);
        }
    }

    private void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }

        if (customerService.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already exists: " + email);
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[0-9].*")) {
            throw new ValidationException("Password must contain at least one digit");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[@#$%^&+=].*")) {
            throw new ValidationException("Password must contain at least one special character");
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            if (!phoneNumber.matches("^\\+?[0-9]{10,15}$")) {
                throw new ValidationException("Invalid phone number format");
            }
        }
    }
}
