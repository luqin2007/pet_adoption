package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 检查记录 - 客观诊断结果关联表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationDiagnosis implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 医疗记录 id
     * *外键:examination(id) 非空 bigint*
     */
    private Long examinationId;

    /**
     * 诊断结果 id
     * *外键:examinationDiagnosis(id) 非空 bigint*
     */
    private Long diagnosisId;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
