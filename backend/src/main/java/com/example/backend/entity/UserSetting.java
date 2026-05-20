package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSetting implements IId {

    public static final UserSetting DEFAULT = new UserSetting();

    private Long id;
    private Long userId;
    private Boolean enableAi = Boolean.TRUE;
    private Date createTime;
    private Date updateTime;

    public void accept(UserSetting request) {
        this.enableAi = request.getEnableAi();
        this.updateTime = new Date();
    }
}
