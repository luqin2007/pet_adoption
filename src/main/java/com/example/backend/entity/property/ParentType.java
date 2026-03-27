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
    REHAB_PLAN("rehab"); // 康复计划

    private final String folder;
}
