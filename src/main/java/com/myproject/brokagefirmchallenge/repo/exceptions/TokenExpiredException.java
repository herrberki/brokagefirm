package com.myproject.brokagefirmchallenge.repo.exceptions;


import org.springframework.http.HttpStatus;

public class TokenExpiredException extends BaseException {

    private static final String ERROR_CODE = "TOKEN_EXPIRED";

    public TokenExpiredException(String message) {
        super(message, ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }
}
