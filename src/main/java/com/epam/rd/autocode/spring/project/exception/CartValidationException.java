package com.epam.rd.autocode.spring.project.exception;

public class CartValidationException extends RuntimeException {
    public CartValidationException(String message) {
        super(message);
    }
}
