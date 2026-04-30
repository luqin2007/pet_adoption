package com.example.backend.component;

import com.example.backend.service.UserService;
import com.example.backend.util.JwtHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterUnitTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void staleTokenForRemovedOrRenamedUserDoesNotEscapeAsServerError() throws Exception {
        JwtHelper jwtHelper = mock(JwtHelper.class);
        UserService userService = mock(UserService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtHelper, userService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/notices");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtHelper.getTokenFromRequest(request)).thenReturn(java.util.Optional.of("token"));
        when(jwtHelper.validateAccessToken("token")).thenReturn(true);
        when(jwtHelper.getUsernameFromToken("token")).thenReturn("old-name");
        when(userService.loadUserByUsername("old-name"))
                .thenThrow(UsernameNotFoundException.fromUsername("old-name"));

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userService).loadUserByUsername("old-name");
    }
}
