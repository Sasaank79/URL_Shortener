package com.urlshortener;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.entity.Url;
import com.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class UrlShortenerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UrlRepository urlRepository;

    @Test
    @DisplayName("End-to-End Flow: Shorten URL -> Persist -> Redirect")
    void shouldShortenAndRedirect() {
        // 1. Shorten URL
        String originalUrl = "https://example.com/integration-test";
        ShortenRequest request = ShortenRequest.builder()
                .originalUrl(originalUrl)
                .build();

        ResponseEntity<ShortenResponse> response = restTemplate.postForEntity(
                "/api/v1/shorten",
                request,
                ShortenResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        String shortCode = response.getBody().getShortCode();
        assertNotNull(shortCode);

        // 2. Verify Database Persistence
        Url savedUrl = urlRepository.findByShortCode(shortCode).orElseThrow();
        assertEquals(originalUrl, savedUrl.getOriginalUrl());

        // 3. Verify Redirect
        ResponseEntity<Void> redirectResponse = restTemplate.getForEntity(
                "/" + shortCode,
                Void.class);

        assertEquals(HttpStatus.FOUND, redirectResponse.getStatusCode());
        assertEquals(originalUrl, redirectResponse.getHeaders().getLocation().toString());
    }
}
