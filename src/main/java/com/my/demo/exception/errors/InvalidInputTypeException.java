package com.my.demo.exception.errors;

public class InvalidInputTypeException extends RuntimeException {
    public InvalidInputTypeException(String message) {
        super(message);
    }
}

