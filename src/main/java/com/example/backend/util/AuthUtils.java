package com.example.backend.util;

import com.example.backend.entity.User;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthUtils {

    private AuthenticationManager authenticationManager;

    /**
     * 获取当前登录用户
     */
    public Optional<User> getLoginUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (CustomUserDetails) auth.getPrincipal())
                .map(CustomUserDetails::getUser);
    }

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     */
    public CustomUserDetails authenticate(String username, String password) {
        Authentication token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(token);
        return (CustomUserDetails) authentication.getPrincipal();
    }

    @Autowired
    @Lazy
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
