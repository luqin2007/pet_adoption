package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.EnumSet;
import java.util.Locale;

/**
 * 志愿者排班状态
 */
public enum VolunteerShiftStatus {
    ASSIGNED, // 已分配
    CONFIRMED(ASSIGNED), // 已确认
    IN_PROGRESS(CONFIRMED), // 执行中
    COMPLETED(CONFIRMED, IN_PROGRESS), // 已完成
    CANCELLED(ASSIGNED, CONFIRMED, IN_PROGRESS), // 已取消
    ABSENT(CONFIRMED); // 缺勤

    private final EnumSet<VolunteerShiftStatus> previousEffect;

    VolunteerShiftStatus(VolunteerShiftStatus first, VolunteerShiftStatus... others) {
        this.previousEffect = EnumSet.of(first, others);
    }

    VolunteerShiftStatus() {
        this.previousEffect = EnumSet.noneOf(VolunteerShiftStatus.class);
    }

    /**
     * 计算排班时需要计算时间是否重叠
     */
    public boolean isTimeEffective() {
        return this == ASSIGNED || this == CONFIRMED || this == IN_PROGRESS;
    }

    /**
     * 该状态只有工作人员可切换
     */
    public boolean requireWorker() {
        return this == IN_PROGRESS || this == COMPLETED || this == CANCELLED || this == ABSENT;
    }

    /**
     * 该状态只有本人可切换
     */
    public boolean requireSelf() {
        return this == CONFIRMED;
    }

    /**
     * 是否可切换
     */
    public boolean canSwitchFrom(VolunteerShiftStatus status) {
        return previousEffect.contains(status);
    }

    /**
     * 根据字符串获取枚举
     */
    public static VolunteerShiftStatus get(String name) {
        try {
            return VolunteerShiftStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("无效排班状态 " + name);
        }
    }
}
