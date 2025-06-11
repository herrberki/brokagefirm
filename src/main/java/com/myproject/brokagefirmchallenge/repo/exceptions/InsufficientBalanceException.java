package com.myproject.brokagefirmchallenge.repo.exceptions;


import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BaseException {

    private static final String ERROR_CODE = "INSUFFICIENT_BALANCE";

    public InsufficientBalanceException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}
