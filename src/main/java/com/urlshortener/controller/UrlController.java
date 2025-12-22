package com.urlshortener.controller;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST Controller for URL shortening operations.
 * 
 * Endpoints:
 * - POST /api/v1/shorten : Create short URL
 * - GET /{shortCode} : Redirect to original URL
 * - GET /api/v1/urls/{code} : Get URL info
 * - GET /api/v1/urls/{code}/stats : Get click statistics
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UrlController {

    private final UrlService urlService;

    /**
     * Create a shortened URL.
     * 
     * Request:
     * {
     * "originalUrl": "https://example.com/very/long/url",
     * "customAlias": "mylink", // optional
     * "expiresInHours": 24 // optional
     * }
     * 
     * Response:
     * {
     * "shortCode": "mylink",
     * "shortUrl": "http://localhost:8080/mylink",
     * "originalUrl": "https://example.com/very/long/url"
     * }
     */
    @PostMapping("/api/v1/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        log.info("Received shorten request for URL: {}", request.getOriginalUrl());
        ShortenResponse response = urlService.shortenUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Redirect to original URL.
     * 
     * This is the main endpoint users will hit.
     * Returns 302 Found with Location header.
     * 
     * Interview talking point:
     * "We use 302 (temporary redirect) instead of 301 (permanent)
     * to ensure browsers don't cache the redirect, allowing us
     * to track click statistics accurately."
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        log.info("Redirect request for short code: {}", shortCode);
        String originalUrl = urlService.getOriginalUrl(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));

        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    /**
     * Get URL information (without incrementing click count).
     */
    @GetMapping("/api/v1/urls/{shortCode}")
    public ResponseEntity<UrlStatsResponse> getUrlInfo(@PathVariable String shortCode) {
        log.info("Info request for short code: {}", shortCode);
        UrlStatsResponse stats = urlService.getUrlStats(shortCode);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get URL statistics including click count.
     */
    @GetMapping("/api/v1/urls/{shortCode}/stats")
    public ResponseEntity<UrlStatsResponse> getUrlStats(@PathVariable String shortCode) {
        log.info("Stats request for short code: {}", shortCode);
        UrlStatsResponse stats = urlService.getUrlStats(shortCode);
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
