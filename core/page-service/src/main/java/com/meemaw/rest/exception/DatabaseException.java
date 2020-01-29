package com.meemaw.rest.exception;

public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException() {
        this("Something went wrong while trying access database, please try again");
    }
}
