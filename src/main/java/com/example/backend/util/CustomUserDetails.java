package com.example.backend.util;

import com.example.backend.entity.User;
import lombok.Getter;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private final User user;

    public CustomUserDetails(User user) {
        super(user.getUsername(), user.getPassword(), UserUtils.createAuthorities(user.getRole()));
        this.user = user;
    }
}
