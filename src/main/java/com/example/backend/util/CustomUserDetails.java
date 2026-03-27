package com.example.backend.util;

import com.example.backend.entity.User;
import com.example.backend.entity.property.UserRole;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.stream.Collectors;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private final User user;

    public CustomUserDetails(User user) {
        super(user.getUsername(), user.getPassword(), UserRole.getRoles(user.getRole())
                .map(UserRole::getRole)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet()));
        this.user = user;
    }
}
