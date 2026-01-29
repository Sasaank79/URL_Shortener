package com.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.ShortenResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.exception.AliasAlreadyExistsException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UrlService urlService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("POST /api/v1/shorten - Success")
        void shortenUrl_Success() throws Exception {
                ShortenRequest request = new ShortenRequest();
                request.setOriginalUrl("https://example.com");

                ShortenResponse response = ShortenResponse.builder()
                                .shortCode("abc1234")
                                .shortUrl("http://localhost:8080/abc1234")
                                .originalUrl("https://example.com")
                                .createdAt(LocalDateTime.now())
                                .build();

                when(urlService.shortenUrl(any(ShortenRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/v1/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.shortCode").value("abc1234"))
                                .andExpect(jsonPath("$.originalUrl").value("https://example.com"));
        }

        @Test
        @DisplayName("GET /{shortCode} - Success (Redirect)")
        void redirect_Success() throws Exception {
                when(urlService.getOriginalUrl("abc1234")).thenReturn("https://example.com");

                mockMvc.perform(get("/abc1234"))
                                .andExpect(status().isFound())
                                .andExpect(header().string("Location", "https://example.com"));
        }

        @Test
        @DisplayName("GET /{shortCode} - Not Found")
        void redirect_NotFound() throws Exception {
                when(urlService.getOriginalUrl("notfound")).thenThrow(new UrlNotFoundException("notfound"));

                mockMvc.perform(get("/notfound"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("URL not found for short code: notfound"));
        }

        @Test
        @DisplayName("POST /api/v1/shorten - Alias Already Exists")
        void shortenUrl_AliasExists() throws Exception {
                ShortenRequest request = new ShortenRequest();
                request.setOriginalUrl("https://example.com");
                request.setCustomAlias("exists");

                when(urlService.shortenUrl(any(ShortenRequest.class)))
                                .thenThrow(new AliasAlreadyExistsException("exists"));

                mockMvc.perform(post("/api/v1/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").value("Conflict"));
        }

        @Test
        @DisplayName("GET /api/v1/urls/{shortCode}/stats - Success")
        void getUrlStats_Success() throws Exception {
                UrlStatsResponse stats = UrlStatsResponse.builder()
                                .shortCode("abc1234")
                                .originalUrl("https://example.com")
                                .clickCount(42L)
                                .build();

                when(urlService.getUrlStats("abc1234")).thenReturn(stats);

                mockMvc.perform(get("/api/v1/urls/abc1234/stats"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.clickCount").value(42));
        }
}
