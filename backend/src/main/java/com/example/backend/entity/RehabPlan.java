package com.example.backend.entity;

import com.example.backend.entity.property.RehabPlanStatusProp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RehabPlan implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 兽医 id
     * *外键:user(id) 非空 bigint*
     */
    private Long doctorId;

    /**
     * 流浪宠物 id
     * *外键:pet(id) 非空 bigint*
     */
    private Long petId;

    /**
     * 流浪宠物年龄（月）
     * *非空 int*
     */
    private Integer petAge;

    /**
     * 标题
     * *非空 varchar(255)*
     */
    private String title;

    /**
     * 内容
     * *非空 text*
     */
    private String content;

    /**
     * 执行频率
     * *非空 varchar(255)*
     */
    private String frequency;

    /**
     * 状态
     * *非空 tinyint*
     */
    private RehabPlanStatusProp status;

    /**
     * 开始时间
     * *非空 datetime*
     */
    private Date startTime;

    /**
     * 预计结束时间
     * *非空 datetime*
     */
    private Date endTime;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
