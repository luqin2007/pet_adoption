package com.example.backend.util;

import com.example.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access-expire-seconds}")
    private long accessExpireSeconds;
    @Value("${jwt.refresh-expire-seconds:604800}")
    private long refreshExpireSeconds;

    private SecretKey secretKey;
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String INVALID_TOKEN_KEY_TEMPLATE = "pet_adoption.invalid_token.%s";

    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret 至少需要 32 字节");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        return generateToken(user, accessExpireSeconds, ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshExpireSeconds, REFRESH_TOKEN_TYPE);
    }

    private String generateToken(User user, long expireSeconds, String tokenType) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, ACCESS_TOKEN_TYPE);
    }

    public boolean validateRefreshToken(String token) {
        if (redisTemplate.opsForValue().get(String.format(INVALID_TOKEN_KEY_TEMPLATE, token)) != null)
            return false;
        return validateToken(token, REFRESH_TOKEN_TYPE);
    }

    private boolean validateToken(String token, String expectedType) {
        if (!StringUtils.hasText(token))
            return false;
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            return Objects.equals(expectedType, tokenType);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void invalidateRefreshToken(String token) {
        redisTemplate.opsForValue().set(String.format(INVALID_TOKEN_KEY_TEMPLATE, token), "", refreshExpireSeconds, TimeUnit.SECONDS);
    }
}
