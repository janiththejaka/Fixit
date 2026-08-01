package com.fixit.platform.modules.auth.service;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(secretKey)
        );
    }
    public String generateToken(UUID userId, String email, List<String> roles) {

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId.toString())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60))
                .signWith(getSigningKey())
                .compact();
    }

    public UUID extractUserId(String token) {
        String userId = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", String.class);

        return UUID.fromString(userId);
    }

    public List<String> extractRoles(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object rolesObject = claims.get("roles");

        if (rolesObject instanceof List<?>) {
            return ((List<?>) rolesObject)
                    .stream()
                    .map(Object::toString)
                    .toList();
        }

        return List.of();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token) {

        try {

            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (ExpiredJwtException e) {

            throw new RuntimeException("Token expired");

        } catch (JwtException e) {

            throw new RuntimeException("Invalid token");
        }
    }
}
