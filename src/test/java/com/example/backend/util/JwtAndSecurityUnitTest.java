package com.example.backend.util;

import com.example.backend.entity.User;
import com.example.backend.entity.property.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JwtAndSecurityUnitTest {

    private StringRedisTemplate redisTemplate;
    private JwtHelper jwtHelper;
    private User user;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        jwtHelper = new JwtHelper(redisTemplate);
        ReflectionTestUtils.setField(jwtHelper, "secret", "pet-adoption-test-secret-key-at-least-32-bytes");
        ReflectionTestUtils.setField(jwtHelper, "accessExpireSeconds", 1800L);
        ReflectionTestUtils.setField(jwtHelper, "refreshExpireSeconds", 3600L);
        ReflectionTestUtils.setField(jwtHelper, "jwtHeader", "Authorization");
        ReflectionTestUtils.setField(jwtHelper, "jwtPrefix", "Bearer");
        ReflectionTestUtils.setField(jwtHelper, "validateKey", "invalid:%s");
        jwtHelper.init();

        user = new User();
        user.setId(42L);
        user.setUsername("alice");
        user.setPassword("{noop}password");
        user.setRole(UserRole.WORKER.getSetMask());
    }

    @Test
    void generatedAccessAndRefreshTokensValidateOnlyAsTheirOwnType() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        String accessToken = jwtHelper.generateAccessToken(user);
        String refreshToken = jwtHelper.generateRefreshToken(user);

        assertEquals("alice", jwtHelper.getUsernameFromToken(accessToken));
        assertTrue(jwtHelper.validateAccessToken(accessToken));
        assertFalse(jwtHelper.validateRefreshToken(accessToken));
        assertTrue(jwtHelper.validateRefreshToken(refreshToken));
        assertFalse(jwtHelper.validateAccessToken(refreshToken));
    }

    @Test
    void invalidatedAccessTokenIsStoredUntilJwtExpirationAndThenRejected() {
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        String token = jwtHelper.generateAccessToken(user);

        jwtHelper.invalidateAccessToken(token);
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        verify(values).set(eq("invalid:" + token), eq(""), longThat(ttl -> ttl > 0), eq(TimeUnit.MILLISECONDS));
        assertFalse(jwtHelper.validateAccessToken(token));
    }

    @Test
    void getTokenFromRequestRequiresConfiguredBearerHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer abc.def.ghi");

        assertEquals("abc.def.ghi", jwtHelper.getTokenFromRequest(request).orElseThrow());

        when(request.getHeader("Authorization")).thenReturn("Basic abc");
        assertTrue(jwtHelper.getTokenFromRequest(request).isEmpty());

        when(request.getHeader("Authorization")).thenReturn("Bearer   ");
        assertTrue(jwtHelper.getTokenFromRequest(request).isEmpty());
    }

    @Test
    void customUserDetailsMapsBitmaskRolesToSpringAuthorities() {
        CustomUserDetails details = new CustomUserDetails(user);

        Set<String> authorities = details.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        assertSame(user, details.getUser());
        assertEquals("alice", details.getUsername());
        assertTrue(authorities.contains("ROLE_VOLUNTEER"));
        assertTrue(authorities.contains("ROLE_WORKER"));
        assertFalse(authorities.contains("ROLE_ADMIN"));
    }
}
