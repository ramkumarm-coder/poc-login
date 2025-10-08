package com.insp.poc_login.exception;

public class InvalidUserCreationException extends RuntimeException{

    public InvalidUserCreationException() {
        super("User already exist!");
    }

    public InvalidUserCreationException(String message) {
        super(message);
    }
}
