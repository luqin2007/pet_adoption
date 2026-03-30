package com.example.backend.entity.property;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
    AGREEMENT_UPDATE("agreement_update"), // 协议备份
    FOLLOW("follow"), // 领养跟踪
    ;

    private final String folder;
}
