package com.myproject.brokagefirmchallenge.repo.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidOrderException extends BaseException {

    private static final String ERROR_CODE = "INVALID_ORDER";

    public InvalidOrderException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public InvalidOrderException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}
