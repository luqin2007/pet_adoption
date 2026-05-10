package com.example.backend.entity.property;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum UserRole {

    // 爱心人士 (普通会员)
    NORMAL("ROLE_NORMAL", 0x0),
    // 志愿者
    VOLUNTEER("ROLE_VOLUNTEER", 0x1),
    // 救助站工作人员 (工作人员 + 志愿者)
    WORKER("ROLE_WORKER", 0x2),
    // 捐赠者
    DONOR("ROLE_DONOR", 0x4),
    // 兽医
    DOCTOR("ROLE_DOCTOR", 0x8),
    // 超级管理员 (ALL)
    ADMIN("ROLE_ADMIN", 0x10);

    public static final int MAX_ROLE = 0x1F;

    private final String role;
    private final int mask;  // 权限分配/校验时使用

    public static Stream<UserRole> getRoles(int mask) {
        if ((mask & ADMIN.mask) == ADMIN.mask) {
            return Stream.of(values());
        }

        EnumSet<UserRole> roles = EnumSet.of(NORMAL);
        if ((mask & WORKER.mask) == WORKER.mask) {
            roles.add(WORKER);
            roles.add(VOLUNTEER);
        } else if ((mask & VOLUNTEER.mask) == VOLUNTEER.mask) {
            roles.add(VOLUNTEER);
        }
        if ((mask & DONOR.mask) == DONOR.mask) {
            roles.add(DONOR);
        }
        if ((mask & DOCTOR.mask) == DOCTOR.mask) {
            roles.add(DOCTOR);
        }
        return Stream.of(values()).filter(roles::contains);
    }

    public static Integer rezip(int mask) {
        return getRoles(mask)
                .mapToInt(role -> role.mask)
                .reduce(0, (a, b) -> a | b);
    }

    public boolean match(Integer bit) {
        return bit != null && getRoles(bit).anyMatch(this::equals);
    }
}
