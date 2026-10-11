package com.example.lostfound.controller;

import com.example.lostfound.security.AdminTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    // Set these in application-local.properties (not committed to GitHub)
    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:}")
    private String adminPassword;

    @Autowired
    private AdminTokenStore tokens;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        // Login stays disabled until a password is configured
        if (adminPassword == null || adminPassword.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Admin login is not configured on the server"));
        }

        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");

        if (!sameText(username, adminUsername) || !sameText(password, adminPassword)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Wrong username or password"));
        }

        return ResponseEntity.ok(Map.of("token", tokens.createToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokens.remove(authHeader.substring(7));
        }
        return ResponseEntity.noContent().build();
    }

    // Lets the frontend check that a saved token is still valid
    @GetMapping("/check")
    public ResponseEntity<?> check(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")
                && tokens.isValid(authHeader.substring(7))) {
            return ResponseEntity.ok(Map.of("admin", true));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Admin login required"));
    }

    // Compares texts in constant time
    private boolean sameText(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}