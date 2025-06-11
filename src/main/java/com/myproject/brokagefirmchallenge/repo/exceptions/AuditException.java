package com.myproject.brokagefirmchallenge.repo.exceptions;

import org.springframework.http.HttpStatus;

public class AuditException extends BaseException {

    private static final String ERROR_CODE = "AUDIT_ERROR";

    public AuditException(String message) {
        super(message, ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public AuditException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
