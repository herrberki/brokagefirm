package com.myproject.brokagefirmchallenge.repo.exceptions;


import org.springframework.http.HttpStatus;

public class CustomerNotFoundException extends BaseException {

    private static final String ERROR_CODE = "CUSTOMER_NOT_FOUND";

    public CustomerNotFoundException(String message) {
        super(message, ERROR_CODE, HttpStatus.NOT_FOUND);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.NOT_FOUND);
    }
}
