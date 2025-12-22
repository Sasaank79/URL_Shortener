package com.urlshortener.exception;

/**
 * Exception thrown when a custom alias is already taken.
 */
public class AliasAlreadyExistsException extends RuntimeException {

    public AliasAlreadyExistsException(String alias) {
        super("Custom alias already exists: " + alias);
    }
}
