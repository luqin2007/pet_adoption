package com.example.backend.util;

import com.example.backend.entity.User;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.stream.Collectors;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private final User user;

    public CustomUserDetails(User user) {
        super(user.getUsername(), user.getPassword(), Bits.unzip2(C.USER_MASK_ROLE_MAP, user.getRole())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet()));
        this.user = user;
    }
}
