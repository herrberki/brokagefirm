package com.myproject.brokagefirmchallenge.repo.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateUsernameException extends BaseException {

    private static final String ERROR_CODE = "DUPLICATE_USERNAME";

    public DuplicateUsernameException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }

    public DuplicateUsernameException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.CONFLICT);
    }
}
