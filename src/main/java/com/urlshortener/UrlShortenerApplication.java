package com.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * URL Shortener Application
 * 
 * A high-performance URL shortening service using:
 * - Spring Boot for REST API
 * - Redis for caching (sub-millisecond lookups)
 * - PostgreSQL for persistent storage
 * - Base62 encoding for short codes
 */
@SpringBootApplication
@EnableCaching
public class UrlShortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }
}
