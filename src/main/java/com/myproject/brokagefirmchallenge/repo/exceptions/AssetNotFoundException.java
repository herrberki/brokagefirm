package com.myproject.brokagefirmchallenge.repo.exceptions;


import org.springframework.http.HttpStatus;

public class AssetNotFoundException extends BaseException {

    private static final String ERROR_CODE = "ASSET_NOT_FOUND";

    public AssetNotFoundException(String message) {
        super(message, ERROR_CODE, HttpStatus.NOT_FOUND);
    }

    public AssetNotFoundException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE, HttpStatus.NOT_FOUND);
    }
}
