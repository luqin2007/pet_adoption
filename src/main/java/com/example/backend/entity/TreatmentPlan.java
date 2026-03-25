package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 治疗计划
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentPlan implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 兽医 id
     * *外键:user(id) 非空 int*
     */
    private Long doctorId;

    /**
     * 病历 id
     * *外键:medicalDetail(id) int*
     */
    private Long detailId;

    /**
     * 治疗计划
     * *非空 text*
     */
    private String plan;

    /**
     * 开始时间
     * *非空 timestamp*
     */
    private Date startTime;

    /**
     * 结束时间，留空表示长期
     * *timestamp*
     */
    private Date endTime;

    /**
     * 创建时间
     * *非空 timestamp*
     */
    private Date createTime;

    /**
     * 废弃状态
     * *非空 boolean*
     */
    private Boolean isDiscard;
}
