package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * Request DTO for URL shortening.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortenRequest {

    @NotBlank(message = "URL cannot be blank")
    @URL(message = "Must be a valid URL")
    private String originalUrl;

    /**
     * Optional custom alias for the short URL.
     * If not provided, a Base62 encoded short code will be generated.
     */
    private String customAlias;

    /**
     * Optional expiration time in hours.
     * If not provided, the URL will not expire.
     */
    private Integer expiresInHours;
}
