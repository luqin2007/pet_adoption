package com.example.backend.component;

import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtils;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        jwtUtils.getTokenFromRequest(request)
                // 校验 token
                .filter(jwtUtils::validateAccessToken)
                // 设置认证信息
                .ifPresent(token -> setAuthentication(token, request));
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token, HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null) {
            String username = jwtUtils.getUsernameFromToken(token);
            UserDetails userDetails = userService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            context.setAuthentication(authentication);
        }
    }
}
