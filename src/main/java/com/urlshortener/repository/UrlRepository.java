package com.urlshortener.repository;

import com.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for URL entity operations.
 * 
 * Design decisions:
 * - Custom query for atomic click count increment (thread-safe)
 * - Index on shortCode ensures O(1) lookups
 */
@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    /**
     * Find URL by short code.
     * Leverages the database index for fast lookups.
     */
    Optional<Url> findByShortCode(String shortCode);

    /**
     * Check if a short code already exists.
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Atomically increment the click count.
     * This is thread-safe and avoids race conditions.
     * 
     * Interview talking point:
     * "We use database-level atomic increment to handle concurrent
     * clicks without race conditions, instead of read-modify-write
     * which could lose updates under high load."
     */
    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    int incrementClickCount(@Param("shortCode") String shortCode);
}
