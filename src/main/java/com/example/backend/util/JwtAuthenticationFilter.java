package com.example.backend.util;

import com.example.backend.service.UserManagerService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserManagerService userManagerService;

    @Value("${jwt.header}")
    private String jwtHeader;
    @Value("${jwt.prefix}")
    private String jwtPrefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        // 获取 Token
        String header = request.getHeader(jwtHeader);
        if (!StringUtils.hasText(header)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!header.startsWith(jwtPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(jwtPrefix.length()).trim();
        if (!StringUtils.hasText(token) || !jwtUtils.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 重建认证对象
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null) {
            String username = jwtUtils.getUsernameFromToken(token);
            UserDetails userDetails = userManagerService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            context.setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
