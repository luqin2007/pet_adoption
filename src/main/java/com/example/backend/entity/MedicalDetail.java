package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 病历
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDetail implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 就诊记录 id
     * *外键:medicalRecord(id) 非空 int*
     */
    private Long recordId;

    /**
     * 兽医 id
     * *外键:user(id) 非空 int*
     */
    private Long doctorId;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 更新时间
     * *非空 datetime*
     */
    private Date updateTime;

    /**
     * 是否已完成
     * *非空 boolean*
     */
    private Boolean isCompleted;

    /**
     * 是否已废弃
     * *非空 boolean*
     */
    private Boolean isDiscard;

    /**
     * 摘要，用于显示在列表中
     * *varchar(255)*
     */
    private String summary;

    // ---- S 主观信息

    /**
     * 问题描述
     * *非空 text*
     */
    private String description;

    /**
     * 现病史
     * *非空 text*
     */
    private String history;

    /**
     * 既往史
     * *非空 text*
     */
    private String pastHistory;

    /**
     * 生活习性
     * *非空 text*
     */
    private String lifeHabit;

    // ---- O 客观检查 其他复杂检查信息存于 ObjectiveDiagnosis 表中

    /**
     * 宠物体重 kg
     * *非空 decimal(6,2)*
     */
    private Double weight;

    /**
     * 宠物体温 ℃
     * *非空 decimal(3,2)*
     */
    private Double temperature;

    /**
     * 心率
     * *非空 integer*
     */
    private Integer heartRate;

    /**
     * 呼吸频率
     * *非空 integer*
     */
    private Integer respiratoryRate;

    /**
     * 其他体检信息
     * *非空 text*
     */
    private String physicalExam;

    // ---- A 评估诊断

    /**
     * 诊断结果
     * *text*
     */
    private String diagnosis;

    /**
     * 鉴别诊断
     * *text*
     */
    private String differential;

    // ---- P 治疗方案 具体治疗方案存于 Treatment 表中

    /**
     * 检查计划
     * *text*
     */
    private String exam;

    /**
     * 治疗方案 概括
     * *text*
     */
    private String treatment;

    /**
     * 医嘱
     * *text*
     */
    private String advice;
}
