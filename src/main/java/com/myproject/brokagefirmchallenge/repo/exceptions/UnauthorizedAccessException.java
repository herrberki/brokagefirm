package com.myproject.brokagefirmchallenge.repo.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends BaseException {

    private static final String ERROR_CODE = "UNAUTHORIZED_ACCESS";

    public UnauthorizedAccessException(String message) {
        super(message, ERROR_CODE, HttpStatus.FORBIDDEN);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.FORBIDDEN);
    }
}
