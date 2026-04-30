package com.example.backend.util;

import com.example.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * JWT 工具类
 */
@Component
@RequiredArgsConstructor
public class JwtHelper {

    public static final String TOKEN_CLAIM_TYPE_KEY = "tokenType";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access-expire-seconds}")
    private long accessExpireSeconds;
    @Value("${jwt.refresh-expire-seconds:604800}")
    private long refreshExpireSeconds;
    @Value("${jwt.header}")
    private String jwtHeader;
    @Value("${jwt.prefix}")
    private String jwtPrefix;
    @Value("${key.token_invalid}")
    private String validateKey;

    private SecretKey secretKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtHelper.class);

    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            int diff = 32 - keyBytes.length;
            secret = secret + StringUtils.generateRandomString(diff);
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            LOGGER.warn("jwt.secret 长度不足，运行时已自动补全 {} 字节", diff);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        return generateToken(user, accessExpireSeconds, TOKEN_TYPE_ACCESS);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshExpireSeconds, TOKEN_TYPE_REFRESH);
    }

    private String generateToken(User user, long expireSeconds, String tokenType) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .claim(TOKEN_CLAIM_TYPE_KEY, tokenType)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, TOKEN_TYPE_ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, TOKEN_TYPE_REFRESH);
    }

    private boolean validateToken(String token, String expectedType) {
        if (!StringUtils.hasText(token))
            return false;
        if (isTokenInvalidated(token))
            return false;
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get(TOKEN_CLAIM_TYPE_KEY, String.class);
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

    public void invalidateAccessToken(String token) {
        invalidateToken(token, TOKEN_TYPE_ACCESS);
    }

    public void invalidateRefreshToken(String token) {
        invalidateToken(token, TOKEN_TYPE_REFRESH);
    }

    private void invalidateToken(String token, String expectedType) {
        if (!StringUtils.hasText(token))
            return;
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get(TOKEN_CLAIM_TYPE_KEY, String.class);
            if (!Objects.equals(expectedType, tokenType))
                return;

            Date expireAt = claims.getExpiration();
            long ttl = expireAt.getTime() - System.currentTimeMillis();
            if (ttl <= 0) {
                return;
            }
            String redisKey = String.format(validateKey, token);
            redisTemplate.opsForValue().set(redisKey, "", ttl, TimeUnit.MILLISECONDS);
        } catch (JwtException | IllegalArgumentException ignored) {
        }
    }

    private boolean isTokenInvalidated(String token) {
        String redisKey = String.format(validateKey, token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }

    public Optional<String> getTokenFromRequest(HttpServletRequest request) {
        // 获取 Token
        String header = request.getHeader(jwtHeader);
        if (!StringUtils.hasText(header) || !header.startsWith(jwtPrefix)) {
            return Optional.empty();
        }
        String token = header.substring(jwtPrefix.length()).trim();
        return StringUtils.hasText(token) ? Optional.of(token) : Optional.empty();
    }
}
