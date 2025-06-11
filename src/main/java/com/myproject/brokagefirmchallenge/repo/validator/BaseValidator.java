package com.myproject.brokagefirmchallenge.repo.validator;

import com.myproject.brokagefirmchallenge.repo.exceptions.ValidationException;

public abstract class BaseValidator<T> {

    public abstract void validate(T request);

    protected void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }
    }

    protected void validatePositive(Number value) {
        if (value != null && value.doubleValue() <= 0) {
            throw new ValidationException("Transfer amount" + " must be positive");
        }
    }

    protected void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }
}
