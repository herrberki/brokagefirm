package com.myproject.brokagefirmchallenge.repo.exceptions;

import org.springframework.http.HttpStatus;

public class ConcurrencyException extends BaseException {

    private static final String ERROR_CODE = "CONCURRENCY_ERROR";

    public ConcurrencyException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }

    public ConcurrencyException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.CONFLICT);
    }
}
