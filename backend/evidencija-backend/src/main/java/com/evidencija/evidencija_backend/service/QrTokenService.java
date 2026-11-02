package com.evidencija.evidencija_backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class QrTokenService {

    private final SecretKey signingKey;

    public QrTokenService(@Value("${qr.token.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long sessionId, long validitySeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validitySeconds * 1000);

        return Jwts.builder()
                .claim("sessionId", sessionId)
                .claim("nonce", java.util.UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public Long validateAndExtractSessionId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("sessionId", Long.class);
    }
}