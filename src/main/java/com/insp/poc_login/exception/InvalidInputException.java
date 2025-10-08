package com.insp.poc_login.exception;

public class InvalidInputException extends RuntimeException{
    public InvalidInputException() {
        super("Input is invalid");
    }

    public InvalidInputException(String message) {
        super(message);
    }
}
