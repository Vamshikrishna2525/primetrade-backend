package com.primetrade.security;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final Key signingKey;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes;
        // support both base64 and plain secrets
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception e) {
            keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (token == null || token.isBlank()) return false;
        final String username = getUsernameFromToken(token);
        return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        Date exp = extractExpiration(token);
        return exp != null && exp.before(new Date());
    }
}
