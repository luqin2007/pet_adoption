package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Locale;

@Getter
@AllArgsConstructor
public enum ParentType {
    USER("user"), // 用户
    PET("pet"), // 流浪宠物
    RESCUE_TASK("rescue"), // 救助任务
    EXAMINATION("exam"), // 医疗检查
    TREATMENT_PLAN("treatment"), // 治疗计划
    REHAB_PLAN("rehab"), // 康复计划
    ADOPT("adopt"), // 领养
    BREADING("breading"), // 寄养
    AGREEMENT("agreement"), // 领养/寄养协议
    AGREEMENT_RECORD("agreement_record"), // 协议备份
    FOLLOW("follow"), // 领养跟踪
    DONATION("donation"), // 捐赠
    LOST_PET("lost_pet"), // 丢失宠物
    ARTICLE("article"); // 文章

    private final String folder;

    public static ParentType get(String name) {
        try {
            return ParentType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.parent_type");
        }
    }
}
