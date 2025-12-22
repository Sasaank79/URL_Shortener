package com.urlshortener.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Base62Encoder.
 * 
 * These tests verify the encoding/decoding logic that is central
 * to the URL shortening strategy.
 */
class Base62EncoderTest {

    private Base62Encoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new Base62Encoder();
    }

    @Test
    @DisplayName("Encode 0 should return '0'")
    void encode_zero_returnsZero() {
        assertEquals("0", encoder.encode(0));
    }

    @Test
    @DisplayName("Encode 1 should return '1'")
    void encode_one_returnsOne() {
        assertEquals("1", encoder.encode(1));
    }

    @Test
    @DisplayName("Encode 62 should return '10' (Base62)")
    void encode_62_returns10() {
        assertEquals("10", encoder.encode(62));
    }

    @ParameterizedTest
    @ValueSource(longs = { 1, 10, 100, 1000, 10000, 100000, 1000000 })
    @DisplayName("Encode and decode should be reversible")
    void encode_decode_isReversible(long id) {
        String encoded = encoder.encode(id);
        long decoded = encoder.decode(encoded);
        assertEquals(id, decoded);
    }

    @Test
    @DisplayName("Encode large number produces short string")
    void encode_largeNumber_producesShortString() {
        // 56 billion is the max for 6 chars
        String encoded = encoder.encode(56_800_235_584L);
        assertTrue(encoded.length() <= 7);
    }

    @Test
    @DisplayName("Decode empty string throws exception")
    void decode_emptyString_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> encoder.decode(""));
    }

    @Test
    @DisplayName("Decode null throws exception")
    void decode_null_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> encoder.decode(null));
    }

    @Test
    @DisplayName("Decode invalid character throws exception")
    void decode_invalidChar_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> encoder.decode("abc!"));
    }

    @Test
    @DisplayName("isValid returns true for valid Base62 strings")
    void isValid_validString_returnsTrue() {
        assertTrue(encoder.isValid("abc123XYZ"));
    }

    @Test
    @DisplayName("isValid returns false for invalid characters")
    void isValid_invalidChars_returnsFalse() {
        assertFalse(encoder.isValid("abc-123"));
        assertFalse(encoder.isValid("abc 123"));
        assertFalse(encoder.isValid("abc!@#"));
    }

    @Test
    @DisplayName("isValid returns false for empty/null")
    void isValid_emptyOrNull_returnsFalse() {
        assertFalse(encoder.isValid(""));
        assertFalse(encoder.isValid(null));
    }
}
