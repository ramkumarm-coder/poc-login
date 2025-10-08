package com.insp.poc_login.exception;

public class InvalidUserException extends RuntimeException{
    public InvalidUserException() {
        super("Invalid user!");
    }

    public InvalidUserException(String message) {
        super(message);
    }
}
