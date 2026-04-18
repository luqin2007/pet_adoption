package com.example.backend.util;

import com.example.backend.entity.property.*;

public final class NoticeTextUtils {

    private NoticeTextUtils() {
    }

    public static String petStatus(PetStatus status) {
        return switch (status) {
            case WAITING -> "待审核";
            case AGAINST -> "审核未通过";
            case FINDING -> "查找中";
            case DIED -> "无法救助或已死亡";
            case TIMEOUT -> "超时放弃";
            case SHELTERED -> "已收容";
            case HEALTH -> "可领养";
            case ADOPTED -> "已领养";
            case HOME -> "已回家";
        };
    }

    public static String rescueTaskStatus(RescueTaskStatus status) {
        return switch (status) {
            case CREATED -> "已创建";
            case APPROVED -> "审核通过";
            case PROCESSING -> "处理中";
            case COMPLETED -> "已完成";
            case DISCARDED -> "已废弃";
        };
    }

    public static String volunteerShiftStatus(VolunteerShiftStatus status) {
        return switch (status) {
            case ASSIGNED -> "已分配";
            case CONFIRMED -> "已确认";
            case IN_PROGRESS -> "执行中";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
            case ABSENT -> "缺勤";
        };
    }

    public static String volunteerRecordStatus(VolunteerRecordStatus status) {
        return switch (status) {
            case DRAFT -> "草稿";
            case SUBMITTED -> "已提交";
            case APPROVED -> "审核通过";
            case REJECTED -> "审核拒绝";
        };
    }
}
