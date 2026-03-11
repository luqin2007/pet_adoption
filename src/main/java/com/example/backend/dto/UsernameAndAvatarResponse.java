package com.example.backend.dto;

import com.example.backend.entity.IId;
import com.example.backend.entity.User;
import com.example.backend.util.C;
import com.example.backend.util.FileUtils;
import lombok.Data;

import java.util.Map;

/**
 * 仅返回用户 id、用户名、头像，用于显示
 */
@Data
public class UsernameAndAvatarResponse implements IId {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;

    public static UsernameAndAvatarResponse fromEntity(User user) {
        UsernameAndAvatarResponse response = new UsernameAndAvatarResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setAvatar(FileUtils.generateAssetUrl(C.PARENT_USER_AVATAR, user.getId(), user.getAvatar()));
        return response;
    }
}
