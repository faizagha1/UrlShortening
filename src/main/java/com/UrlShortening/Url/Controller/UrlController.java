package com.UrlShortening.Url.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.UrlShortening.Exceptions.GlobalExceptionHandler.UrlNotFoundException;
import com.UrlShortening.Url.Dtos.UrlRequest;
import com.UrlShortening.Url.Dtos.UrlResponse;
import com.UrlShortening.Url.Service.UrlService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/shorten")
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;

    @PostMapping()
    public ResponseEntity<UrlResponse> shortenUrl(
            @Valid @RequestBody UrlRequest urlRequest) {
        // Handle invalid URL response
        if (!urlService.isValidUrl(urlRequest.getOriginalUrl())) {
            return ResponseEntity.badRequest()
                    .body(new UrlResponse(null, "Invalid URL", null, null, null, null));
        }

        return ResponseEntity.ok(urlService.shortenUrl(urlRequest));
    }

    @GetMapping("/original/{shortUrl}")
    public ResponseEntity<String> getOriginalUrl(
            @PathVariable String shortUrl) {
        try {
            return ResponseEntity.ok(urlService.getOriginalUrl(shortUrl));
        } catch (UrlNotFoundException ex) {
            return ResponseEntity.status(404).body("URL not found");
        }
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @PathVariable String shortUrl) {
        try {
            String originalUrl = urlService.getOriginalUrl(shortUrl);
            return ResponseEntity.status(302)
                    .header("Location", originalUrl)
                    .build();
        } catch (UrlNotFoundException ex) {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/{shortUrl}/stats")
    public ResponseEntity<UrlResponse> getStats(
            @PathVariable String shortUrl) {
        try {
            return ResponseEntity.ok(urlService.getStats(shortUrl));
        } catch (UrlNotFoundException ex) {
            return ResponseEntity.status(404).build();
        }
    }

    @PutMapping("/{shortUrl}")
    public ResponseEntity<UrlResponse> updateStats(
            @RequestBody String newUrl,
            @PathVariable String shortUrl) {
        try {
            return ResponseEntity.ok(urlService.updateUrl(shortUrl, newUrl));
        } catch (UrlNotFoundException ex) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @DeleteMapping("/{shortUrl}")
    public ResponseEntity<String> deleteUrl(
            @PathVariable String shortUrl) {
        try {
            UrlResponse urlResponse = urlService.deleteUrl(shortUrl);
            return ResponseEntity.ok("Url with shortUrl: " + urlResponse.shortUrl() + " deleted");
        } catch (UrlNotFoundException ex) {
            return ResponseEntity.status(404).body("URL not found");
        }
    }
}
