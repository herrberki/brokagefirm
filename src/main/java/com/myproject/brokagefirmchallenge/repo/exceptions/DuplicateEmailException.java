package com.myproject.brokagefirmchallenge.repo.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends BaseException {

    private static final String ERROR_CODE = "DUPLICATE_EMAIL";

    public DuplicateEmailException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.CONFLICT);
    }
}
