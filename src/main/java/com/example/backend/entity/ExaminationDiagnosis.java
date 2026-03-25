package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 医疗检查结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationDiagnosis implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 病历 id
     * *外键:medicalDetail(id) 非空 int*
     */
    private Long detailId;

    /**
     * 检查结果
     * *非空 varchar(255)*
     */
    private String result;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 已废弃
     * *非空 boolean*
     */
    private Boolean isDiscard;
}
