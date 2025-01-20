package com.UrlShortening.Url.Dtos;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UrlRequest {
    @NotBlank(message = "Original URL is required")
    @URL(message = "Invalid URL")
    private String originalUrl;
}
