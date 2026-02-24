package com.example.backend.bean;

import com.example.backend.entity.User;
import lombok.Data;

@Data
public class UserLoginResponse {

    private String username;
    private String email;
    private String avatar;
    private String accessToken;
    private String refreshToken;

    public static UserLoginResponse fromEntity(User user) {
        UserLoginResponse response = new UserLoginResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setAccessToken(null);
        response.setRefreshToken(null);
        return response;
    }

    public static UserLoginResponse fromEntity(User user, String accessToken, String refreshToken) {
        UserLoginResponse response = new UserLoginResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }
}
