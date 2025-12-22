package com.urlshortener.exception;

/**
 * Exception thrown when a URL has expired.
 */
public class UrlExpiredException extends RuntimeException {

    public UrlExpiredException(String shortCode) {
        super("URL has expired for short code: " + shortCode);
    }
}
