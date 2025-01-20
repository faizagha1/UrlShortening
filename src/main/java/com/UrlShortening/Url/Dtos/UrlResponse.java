package com.UrlShortening.Url.Dtos;

import java.time.LocalDateTime;

public record UrlResponse(
                Long id,
                String originalUrl,
                String shortUrl,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                Integer accessCount) {

}
