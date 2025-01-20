package com.UrlShortening.Url.Service;

import org.springframework.stereotype.Service;

import com.UrlShortening.Exceptions.GlobalExceptionHandler.InvalidUrlException;
import com.UrlShortening.Exceptions.GlobalExceptionHandler.UrlNotFoundException;
import com.UrlShortening.Url.Dtos.UrlRequest;
import com.UrlShortening.Url.Dtos.UrlResponse;
import com.UrlShortening.Url.Model.Url;
import com.UrlShortening.Url.Repository.UrlRepository;
import com.UrlShortening.Url.Utils.ShortUrlGenerator;

import lombok.RequiredArgsConstructor;
import java.net.URL;
import java.util.regex.Pattern;
import java.net.HttpURLConnection;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;

    public UrlResponse shortenUrl(UrlRequest urlRequest) {
        if (!isValidUrl(urlRequest.getOriginalUrl())) {
            throw new InvalidUrlException("Invalid URL: " + urlRequest.getOriginalUrl());
        }

        if (urlRepository.findByOriginalUrl(urlRequest.getOriginalUrl()).isPresent()) {
            throw new InvalidUrlException("URL already exists: " + urlRequest.getOriginalUrl());
        }
        String shortUrl = ShortUrlGenerator.generateShortUrl(urlRequest.getOriginalUrl());
        Url url = urlRepository.save(Url.builder()
                .originalUrl(urlRequest.getOriginalUrl())
                .shortUrl(shortUrl)
                .accessCount(0)
                .build());
        return new UrlResponse(
                url.getId(),
                url.getOriginalUrl(),
                url.getShortUrl(),
                url.getCreatedAt(),
                url.getUpdatedAt(),
                url.getAccessCount());
    }

    public String getOriginalUrl(String shortUrl) {
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("No url with shortUrl: " + shortUrl));
        url.setAccessCount(url.getAccessCount() + 1);
        urlRepository.save(url);
        return url.getOriginalUrl();
    }

    public UrlResponse getStats(String shortUrl) {
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("No url with shortUrl: " + shortUrl));
        return new UrlResponse(
                url.getId(),
                url.getOriginalUrl(),
                url.getShortUrl(),
                url.getCreatedAt(),
                url.getUpdatedAt(),
                url.getAccessCount());
    }

    public UrlResponse updateUrl(String shortUrl, String newUrl) {
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("No url with shortUrl: " + shortUrl));
        url.setOriginalUrl(newUrl);
        urlRepository.save(url);
        return new UrlResponse(
                url.getId(),
                url.getOriginalUrl(),
                url.getShortUrl(),
                url.getCreatedAt(),
                url.getUpdatedAt(),
                url.getAccessCount());
    }

    public UrlResponse deleteUrl(String shortUrl) {
        Url url = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException("No url with shortUrl: " + shortUrl));
        urlRepository.delete(url);
        return new UrlResponse(
                url.getId(),
                url.getOriginalUrl(),
                url.getShortUrl(),
                url.getCreatedAt(),
                url.getUpdatedAt(),
                url.getAccessCount());
    }

    public boolean isValidUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return false;
        }

        String URL_REGEX = "^(https?://)?" + // Protocol (optional)
                "([\\w-]+\\.)+[\\w-]+" + // Domain name and subdomain
                "(:\\d{1,5})?" + // Port (optional)
                "(/[\\w-./?%&=+#]*)?$"; // Path, query params, and fragment

        if (!Pattern.matches(URL_REGEX, urlString)) {
            System.out.println("URL failed regex validation: " + urlString);
            return false;
        }

        if (!urlString.toLowerCase().startsWith("http://") &&
                !urlString.toLowerCase().startsWith("https://")) {
            urlString = "https://" + urlString;
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            connection.connect();
            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode >= 200 && responseCode < 400;

        } catch (Exception e) {
            System.out.println("Error validating URL: " + e.getMessage());
            return false;
        }
    }

}
