package com.example.lostfound.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps the login tokens of logged-in admins in memory.
 * Tokens expire after 8 hours and are lost when the server restarts.
 */
@Component
public class AdminTokenStore {

    private static final long TOKEN_LIFETIME_MS = 8L * 60 * 60 * 1000;

    private final Map<String, Long> tokens = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String createToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(token, System.currentTimeMillis() + TOKEN_LIFETIME_MS);
        return token;
    }

    public boolean isValid(String token) {
        if (token == null) return false;

        Long expiry = tokens.get(token);
        if (expiry == null) return false;

        if (expiry < System.currentTimeMillis()) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    public void remove(String token) {
        if (token != null) tokens.remove(token);
    }
}