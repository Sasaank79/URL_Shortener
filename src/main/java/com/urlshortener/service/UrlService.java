package com.urlshortener.service;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.dto.UrlStatsResponse;

/**
 * Service interface for URL shortening operations.
 */
public interface UrlService {

    /**
     * Create a shortened URL.
     *
     * @param request The shorten request containing the original URL
     * @return The shorten response with short code and URLs
     */
    ShortenResponse shortenUrl(ShortenRequest request);

    /**
     * Get the original URL for a short code.
     * Also increments the click count.
     *
     * @param shortCode The short code to look up
     * @return The original URL
     */
    String getOriginalUrl(String shortCode);

    /**
     * Get statistics for a shortened URL.
     *
     * @param shortCode The short code to get stats for
     * @return The URL statistics
     */
    UrlStatsResponse getUrlStats(String shortCode);
}
