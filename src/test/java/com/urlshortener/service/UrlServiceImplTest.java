package com.urlshortener.service;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.entity.Url;
import com.urlshortener.exception.AliasAlreadyExistsException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UrlServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class UrlServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private Base62Encoder base62Encoder;

    @InjectMocks
    private UrlServiceImpl urlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("shortenUrl creates URL with generated short code")
    void shortenUrl_noCustomAlias_generatesShortCode() {
        // Arrange
        ShortenRequest request = ShortenRequest.builder()
                .originalUrl("https://example.com/long/url")
                .build();

        Url savedUrl = Url.builder()
                .id(1L)
                .shortCode("temp")
                .originalUrl("https://example.com/long/url")
                .createdAt(LocalDateTime.now())
                .build();

        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);
        when(base62Encoder.encode(1L)).thenReturn("1");

        // Act
        ShortenResponse response = urlService.shortenUrl(request);

        // Assert
        assertNotNull(response);
        assertEquals("1", response.getShortCode());
        assertEquals("http://localhost:8080/1", response.getShortUrl());
        assertEquals("https://example.com/long/url", response.getOriginalUrl());
        verify(urlRepository, times(2)).save(any(Url.class));
    }

    @Test
    @DisplayName("shortenUrl with custom alias uses provided alias")
    void shortenUrl_withCustomAlias_usesAlias() {
        // Arrange
        ShortenRequest request = ShortenRequest.builder()
                .originalUrl("https://example.com/long/url")
                .customAlias("mylink")
                .build();

        when(urlRepository.existsByShortCode("mylink")).thenReturn(false);

        Url savedUrl = Url.builder()
                .id(1L)
                .shortCode("mylink")
                .originalUrl("https://example.com/long/url")
                .createdAt(LocalDateTime.now())
                .build();

        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        // Act
        ShortenResponse response = urlService.shortenUrl(request);

        // Assert
        assertEquals("mylink", response.getShortCode());
        assertEquals("http://localhost:8080/mylink", response.getShortUrl());
    }

    @Test
    @DisplayName("shortenUrl with existing alias throws exception")
    void shortenUrl_existingAlias_throwsException() {
        // Arrange
        ShortenRequest request = ShortenRequest.builder()
                .originalUrl("https://example.com/long/url")
                .customAlias("existing")
                .build();

        when(urlRepository.existsByShortCode("existing")).thenReturn(true);

        // Act & Assert
        assertThrows(AliasAlreadyExistsException.class, () -> urlService.shortenUrl(request));
    }

    @Test
    @DisplayName("getUrlStats returns correct statistics")
    void getUrlStats_existingUrl_returnsStats() {
        // Arrange
        Url url = Url.builder()
                .id(1L)
                .shortCode("abc123")
                .originalUrl("https://example.com")
                .clickCount(42L)
                .createdAt(LocalDateTime.now())
                .build();

        when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));

        // Act
        UrlStatsResponse stats = urlService.getUrlStats("abc123");

        // Assert
        assertEquals("abc123", stats.getShortCode());
        assertEquals(42L, stats.getClickCount());
        assertEquals("https://example.com", stats.getOriginalUrl());
    }

    @Test
    @DisplayName("getUrlStats for non-existent URL throws exception")
    void getUrlStats_nonExistent_throwsException() {
        // Arrange
        when(urlRepository.findByShortCode("notfound")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UrlNotFoundException.class, () -> urlService.getUrlStats("notfound"));
    }
}
