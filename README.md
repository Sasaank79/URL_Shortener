# 🔗 High-Performance URL Shortener

A production-grade URL shortening service (bit.ly clone) built with **Java Spring Boot**, **Redis** for caching, and **PostgreSQL** for persistent storage.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![Redis](https://img.shields.io/badge/Redis-7-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)

## ✨ Features

- **URL Shortening**: Convert long URLs to short, shareable links
- **Custom Aliases**: Create memorable custom short codes
- **Click Tracking**: Real-time click count statistics
- **URL Expiration**: Optional expiration for temporary links
- **High Performance**: Sub-millisecond lookups with Redis caching
- **RESTful API**: Clean, documented REST endpoints

## 🏗️ Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌────────────────┐
│   API Client    │────▶│  Spring Boot     │────▶│    Redis       │
│  (Browser/App)  │◀────│  REST API        │◀────│    Cache       │
└─────────────────┘     └──────────────────┘     └────────────────┘
                               │                        │
                               │    Cache Miss          │
                               ▼                        ▼
                        ┌──────────────────┐     ┌────────────────┐
                        │   PostgreSQL     │────▶│  Populate      │
                        │   Database       │     │  Cache         │
                        └──────────────────┘     └────────────────┘
```

### Key Design Decisions

| Component | Choice | Rationale |
|-----------|--------|-----------|
| **Encoding** | Base62 | 6 chars = 56B URLs, URL-safe characters |
| **Caching** | Redis | Sub-ms reads, 24h TTL for hot URLs |
| **Database** | PostgreSQL | ACID compliance, reliable storage |
| **Redirect** | 302 Found | Allows accurate click tracking |

## 🚀 Quick Start

### Prerequisites

- Java 21 (LTS)
- Docker & Docker Compose

> **Note:** Maven is included via the Maven Wrapper (`./mvnw`) - no separate installation needed!

### 1. Start Infrastructure

```bash
# Start PostgreSQL and Redis
docker-compose up -d

# Verify containers are running
docker-compose ps
```

### 2. Run the Application

```bash
# Using Maven Wrapper (recommended)
./mvnw spring-boot:run

# Or build JAR and run
./mvnw clean package
java -jar target/url-shortener-1.0.0.jar
```

### 3. Test the API

```bash
# Create a short URL
curl -X POST http://localhost:8080/api/v1/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.google.com/search?q=very+long+url"}'

# Response:
# {
#   "shortCode": "1",
#   "shortUrl": "http://localhost:8080/1",
#   "originalUrl": "https://www.google.com/search?q=very+long+url"
# }

# Test redirect (follow redirects)
curl -L http://localhost:8080/1

# Get URL statistics
curl http://localhost:8080/api/v1/urls/1/stats
```

## 📚 API Reference

### Create Short URL

```http
POST /api/v1/shorten
Content-Type: application/json

{
  "originalUrl": "https://example.com/very/long/url",  // Required
  "customAlias": "mylink",                              // Optional
  "expiresInHours": 24                                  // Optional
}
```

**Response**: `201 Created`
```json
{
  "shortCode": "mylink",
  "shortUrl": "http://localhost:8080/mylink",
  "originalUrl": "https://example.com/very/long/url",
  "createdAt": "2024-01-15T10:30:00",
  "expiresAt": "2024-01-16T10:30:00"
}
```

### Redirect to Original URL

```http
GET /{shortCode}
```

**Response**: `302 Found` with `Location` header

### Get URL Statistics

```http
GET /api/v1/urls/{shortCode}/stats
```

**Response**: `200 OK`
```json
{
  "shortCode": "mylink",
  "shortUrl": "http://localhost:8080/mylink",
  "originalUrl": "https://example.com/very/long/url",
  "clickCount": 42,
  "createdAt": "2024-01-15T10:30:00",
  "expiresAt": "2024-01-16T10:30:00",
  "isExpired": false
}
```

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## 🔧 Configuration

Key configuration in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/urlshortener
    username: postgres
    password: postgres

  data:
    redis:
      host: localhost
      port: 6379

app:
  base-url: http://localhost:8080
```

## 💡 Interview Talking Points

> **"Designed a URL shortener using Base62 encoding and Redis caching to handle high-read traffic with sub-millisecond latency."**

### System Design Highlights

1. **Base62 Encoding**
   - Maps database ID to short string
   - 6 characters = 56 billion unique URLs
   - Characters: `[0-9a-zA-Z]`

2. **Redis Caching**
   - Read-through pattern
   - 24-hour TTL for hot URLs
   - Sub-millisecond lookups

3. **Atomic Counters**
   - Database-level atomic increment
   - Thread-safe click tracking
   - No race conditions

4. **Clean Architecture**
   - Controller → Service → Repository
   - Separation of concerns
   - Testable design

## 📁 Project Structure

```
src/main/java/com/urlshortener/
├── UrlShortenerApplication.java   # Main application
├── config/
│   └── RedisConfig.java           # Redis configuration
├── controller/
│   └── UrlController.java         # REST endpoints
├── dto/
│   ├── ShortenRequest.java        # Request DTO
│   ├── ShortenResponse.java       # Response DTO
│   └── UrlStatsResponse.java      # Stats DTO
├── entity/
│   └── Url.java                   # JPA entity
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── UrlNotFoundException.java
│   └── UrlExpiredException.java
├── repository/
│   └── UrlRepository.java         # JPA repository
├── service/
│   ├── UrlService.java            # Service interface
│   └── UrlServiceImpl.java        # Service implementation
└── util/
    └── Base62Encoder.java         # Encoding utility
```

## 📄 License

MIT License - feel free to use this for learning and interviews!
