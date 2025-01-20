package com.UrlShortening.Url.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class ShortUrlGenerator {
    private static final String BASE62_CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SHORT_URL_LENGTH = 7;
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?(([\\w-]+\\.)+[\\w-]+)(:[0-9]+)?(/[\\w-./?%&=]*)?$");

    public static String generateShortUrl(String originalUrl) {
        String hash = generateHash(originalUrl);
        String base62 = convertToBase62(hash);
        return padOrTruncate(base62, SHORT_URL_LENGTH);
    }

    public static boolean isValidUrlFormat(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }

    private static String generateHash(String originalUrl) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(originalUrl.getBytes());
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static String convertToBase62(String hash) {
        // Convert first 12 characters of hash to ensure we stay within long bounds
        String subHash = hash.substring(0, 12);
        long number = 0;

        // Parse hex string to long, handling unsigned values
        for (int i = 0; i < subHash.length(); i++) {
            number = (number * 16L + Character.digit(subHash.charAt(i), 16)) & 0x7fffffffffffffffL;
        }

        // Convert to base62
        StringBuilder base62String = new StringBuilder();
        do {
            int remainder = (int) (number % 62L);
            base62String.append(BASE62_CHARSET.charAt(Math.abs(remainder)));
            number /= 62L;
        } while (number > 0);

        return base62String.reverse().toString();
    }

    private static String padOrTruncate(String input, int length) {
        if (input.length() > length) {
            return input.substring(0, length);
        }
        StringBuilder padded = new StringBuilder(input);
        while (padded.length() < length) {
            padded.append(BASE62_CHARSET.charAt(0));
        }
        return padded.toString();
    }
}