package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

public enum NoticeSource {

    PET_ADOPT, // 领养
    AGREEMENT, // 寄养/领养协议
    BREADING, // 寄养
    DONATION, // 捐赠
    FOLLOW_TASK, // 回访
    PET_CLAIM, // 认领
    PET_RECORD, // 流浪宠物
    RESCUE_TASK, // 救助任务
    STOCK, // 库存
    VOLUNTEER; // 志愿活动

    public static NoticeSource get(String name) {
        try {
            return NoticeSource.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.source");
        }
    }
}
