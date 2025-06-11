package com.myproject.brokagefirmchallenge.repo.exceptions;


import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BaseException {

    private static final String ERROR_CODE = "ORDER_NOT_FOUND";

    public OrderNotFoundException(String message) {
        super(message, ERROR_CODE, HttpStatus.NOT_FOUND);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.NOT_FOUND);
    }
}
