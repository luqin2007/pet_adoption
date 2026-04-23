package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum UserRole {

    // 爱心人士 (普通会员)
    NORMAL("ROLE_NORMAL", 0x0, 0x1F),
    // 志愿者
    VOLUNTEER("ROLE_VOLUNTEER", 0x1, 0x1),
    // 救助站工作人员 (工作人员 + 志愿者)
    WORKER("ROLE_WORKER", 0x3, 0x2),
    // 捐赠者
    DONOR("ROLE_DONOR", 0x4, 0x4),
    // 兽医
    DOCTOR("ROLE_DOCTOR", 0x8, 0x8),
    // 超级管理员 (ALL)
    ADMIN("ROLE_ADMIN", 0x1F, 0x0);

    public static final int MAX_ROLE = 0x1F;

    private final String role;
    private final int setMask;   // 权限分配时使用
    private final int matchMask; // 权限校验时使用

    public static Stream<UserRole> getRoles(int mask) {
        return Stream.of(values())
                .filter(role -> (mask & role.matchMask) == role.matchMask);
    }

    public static Integer rezip(int mask) {
        return getRoles(mask)
                .mapToInt(role -> role.setMask)
                .reduce(0, (a, b) -> a | b);
    }

    public static Optional<UserRole> getByRole(String role) {
        if (!StringUtils.hasText(role))
            return Optional.empty();
        try {
            String roleName = role.toUpperCase(Locale.ROOT);
            if (!roleName.startsWith("ROLE_")) // 不确定会不会加 role_ 前缀，先都可以吧
                roleName = "ROLE_" + roleName;
            return Optional.of(UserRole.valueOf(roleName));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean match(Integer bit) {
        return bit != null && (bit & matchMask) == matchMask;
    }
}
