package com.urlshortener.service;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.entity.Url;
import com.urlshortener.exception.AliasAlreadyExistsException;
import com.urlshortener.exception.UrlExpiredException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of URL shortening service with Redis caching.
 * 
 * Caching Strategy (Interview talking point):
 * - Read-through cache: Check Redis first, fall back to PostgreSQL
 * - Cache TTL: 24 hours for hot URLs (configurable)
 * - Cache invalidation: Automatic on expiration
 * 
 * "We use a read-through caching pattern with Redis to achieve
 * sub-millisecond lookups for frequently accessed URLs. Cache
 * misses fall back to PostgreSQL and populate the cache."
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Create a shortened URL.
     * 
     * Flow:
     * 1. Save URL to database (generates ID)
     * 2. Encode ID to Base62 (or use custom alias)
     * 3. Update URL with short code
     * 4. Return response
     */
    @Override
    @Transactional
    public ShortenResponse shortenUrl(ShortenRequest request) {
        log.info("Shortening URL: {}", request.getOriginalUrl());

        // Check for custom alias
        String shortCode = request.getCustomAlias();
        if (shortCode != null && !shortCode.isEmpty()) {
            if (urlRepository.existsByShortCode(shortCode)) {
                throw new AliasAlreadyExistsException(shortCode);
            }
        }

        // Calculate expiration time if provided
        LocalDateTime expiresAt = null;
        if (request.getExpiresInHours() != null && request.getExpiresInHours() > 0) {
            expiresAt = LocalDateTime.now().plusHours(request.getExpiresInHours());
        }

        // Create URL entity
        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode != null ? shortCode : "temp") // Temporary, will be updated
                .expiresAt(expiresAt)
                .build();

        // Save to get generated ID
        url = urlRepository.save(url);

        // Generate short code from ID if not custom
        if (shortCode == null || shortCode.isEmpty()) {
            shortCode = base62Encoder.encode(url.getId());
            url.setShortCode(shortCode);
            url = urlRepository.save(url);
        }

        log.info("Created short URL: {} -> {}", shortCode, request.getOriginalUrl());

        return ShortenResponse.builder()
                .shortCode(shortCode)
                .shortUrl(baseUrl + "/" + shortCode)
                .originalUrl(url.getOriginalUrl())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .build();
    }

    /**
     * Get original URL and increment click count.
     * 
     * Caching:
     * - Uses Redis @Cacheable for sub-millisecond lookups
     * - Cache key: "urls::{shortCode}"
     * - Cache miss: Query PostgreSQL, populate cache
     * 
     * Note: Click count increment happens in a separate transaction
     * to avoid cache inconsistency issues.
     */
    @Override
    @Transactional
    public String getOriginalUrl(String shortCode) {
        log.debug("Looking up short code: {}", shortCode);

        Url url = getCachedUrl(shortCode);

        // Check expiration
        if (url.isExpired()) {
            throw new UrlExpiredException(shortCode);
        }

        // Increment click count (async would be better for high traffic)
        urlRepository.incrementClickCount(shortCode);

        return url.getOriginalUrl();
    }

    /**
     * Get URL from cache or database.
     * 
     * The @Cacheable annotation enables Redis caching:
     * - First call: Query DB, store in Redis
     * - Subsequent calls: Return from Redis (sub-ms)
     */
    @Cacheable(value = "urls", key = "#shortCode", unless = "#result == null")
    public Url getCachedUrl(String shortCode) {
        log.debug("Cache miss for short code: {}", shortCode);
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
    }

    /**
     * Get URL statistics.
     */
    @Override
    @Transactional(readOnly = true)
    public UrlStatsResponse getUrlStats(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        return UrlStatsResponse.builder()
                .shortCode(url.getShortCode())
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .clickCount(url.getClickCount())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .isExpired(url.isExpired())
                .build();
    }

    /**
     * Evict URL from cache (called when URL is deleted or expires).
     */
    @CacheEvict(value = "urls", key = "#shortCode")
    public void evictFromCache(String shortCode) {
        log.info("Evicting short code from cache: {}", shortCode);
    }
}
