package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 健康评估
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthAssessment implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 宠物 id
     * *外键:pet(id) 非空 bigint*
     */
    private Long petId;

    /**
     * 评估者 id
     * *外键:user(id) 非空 bigint*
     */
    private Long assessorId;

    /**
     * 宠物年龄
     * *非空 int*
     */
    private Integer age;

    /**
     * 宠物体重
     * *非空 double*
     */
    private Double weight;

    /**
     * 体况评分
     * *非空 int*
     */
    private Integer scoreBcs;

    /**
     * 精神状态评分
     * *非空 int*
     */
    private Integer scoreMental;

    /**
     * 食欲评分
     * *非空 int*
     */
    private Integer scoreAppetite;

    /**
     * 评估结果
     * *非空 varchar(255)*
     */
    private String summary;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
