package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;

/**
 * 志愿者排班状态
 */
public enum VolunteerShiftStatus {
    ASSIGNED, // 已分配
    CONFIRMED, // 已确认
    IN_PROGRESS, // 执行中
    COMPLETED, // 已完成
    CANCELLED, // 已取消
    ABSENT; // 缺勤

    private static final EnumMap<VolunteerShiftStatus, EnumSet<VolunteerShiftStatus>> PREVIOUS_EFFECTS;

    static {
        PREVIOUS_EFFECTS = new EnumMap<>(VolunteerShiftStatus.class);
        PREVIOUS_EFFECTS.put(ASSIGNED, EnumSet.noneOf(VolunteerShiftStatus.class));
        PREVIOUS_EFFECTS.put(CONFIRMED, EnumSet.of(ASSIGNED));
        PREVIOUS_EFFECTS.put(IN_PROGRESS, EnumSet.of(CONFIRMED));
        PREVIOUS_EFFECTS.put(COMPLETED, EnumSet.of(CONFIRMED, IN_PROGRESS));
        PREVIOUS_EFFECTS.put(CANCELLED, EnumSet.of(ASSIGNED, CONFIRMED, IN_PROGRESS));
        PREVIOUS_EFFECTS.put(ABSENT, EnumSet.of(CONFIRMED));
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
        return PREVIOUS_EFFECTS.get(this).contains(status);
    }

    /**
     * 根据字符串获取枚举
     */
    public static VolunteerShiftStatus get(String name) {
        try {
            return VolunteerShiftStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("exception.invalidate.volunteer_shift_status");
        }
    }
}
