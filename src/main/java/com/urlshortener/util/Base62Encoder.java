package com.urlshortener.util;

import org.springframework.stereotype.Component;

/**
 * Base62 Encoder/Decoder for URL shortening.
 * 
 * Design:
 * - Uses characters [0-9a-zA-Z] (62 characters)
 * - 6 characters = 62^6 = 56.8 billion unique URLs
 * - 7 characters = 62^7 = 3.5 trillion unique URLs
 * 
 * Interview talking point:
 * "We use Base62 encoding to convert numeric database IDs to short,
 * URL-safe strings. This gives us 56 billion unique URLs with just
 * 6 characters, while avoiding confusing characters like 0/O or 1/l."
 */
@Component
public class Base62Encoder {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length(); // 62

    /**
     * Encode a numeric ID to a Base62 string.
     * 
     * @param id The numeric ID to encode
     * @return Base62 encoded string
     */
    public String encode(long id) {
        if (id == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        long num = id;

        while (num > 0) {
            sb.append(ALPHABET.charAt((int) (num % BASE)));
            num /= BASE;
        }

        return sb.reverse().toString();
    }

    /**
     * Decode a Base62 string back to a numeric ID.
     * 
     * @param shortCode The Base62 encoded string
     * @return The original numeric ID
     * @throws IllegalArgumentException if the string contains invalid characters
     */
    public long decode(String shortCode) {
        if (shortCode == null || shortCode.isEmpty()) {
            throw new IllegalArgumentException("Short code cannot be null or empty");
        }

        long id = 0;

        for (char c : shortCode.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid character in short code: " + c);
            }
            id = id * BASE + index;
        }

        return id;
    }

    /**
     * Validate if a string is a valid Base62 encoded value.
     * 
     * @param shortCode The string to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String shortCode) {
        if (shortCode == null || shortCode.isEmpty()) {
            return false;
        }

        for (char c : shortCode.toCharArray()) {
            if (ALPHABET.indexOf(c) == -1) {
                return false;
            }
        }

        return true;
    }
}
