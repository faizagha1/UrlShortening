package com.UrlShortening.Url.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.UrlShortening.Url.Model.Url;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortUrl(String shortUrl);

    Optional<Url> findByOriginalUrl(String originalUrl);

}
