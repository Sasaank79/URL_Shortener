package com.urlshortener.exception;

/**
 * Exception thrown when a URL with the given short code is not found.
 */
public class UrlNotFoundException extends RuntimeException {

    public UrlNotFoundException(String shortCode) {
        super("URL not found for short code: " + shortCode);
    }
}
