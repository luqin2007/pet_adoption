package com.example.backend.bean;

import com.example.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginResponse {

    private String username;
    private String email;
    private String avatar;
    private String token;

    public static UserLoginResponse fromEntity(User user) {
        return new UserLoginResponse(user.getUsername(), user.getEmail(), user.getAvatar(), null);
    }

    public static UserLoginResponse fromEntity(User user, String token) {
        return new UserLoginResponse(user.getUsername(), user.getEmail(), user.getAvatar(), token);
    }
}
