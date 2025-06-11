package com.myproject.brokagefirmchallenge.repo.exceptions;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends BaseException {

    private static final String ERROR_CODE = "AUTHENTICATION_FAILED";

    public AuthenticationException(String message) {
        super(message, ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }
}
