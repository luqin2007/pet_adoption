package com.example.backend.entity;

import com.example.backend.entity.property.VolunteerShiftStatus;
import com.example.backend.entity.property.VolunteerTaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿者排班
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerShift implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 志愿者 id
     * *外键:user(id) 非空 bigint*
     */
    private Long volunteerId;

    /**
     * 排班人 id
     * *外键:user(id) 非空 bigint*
     */
    private Long assignerId;

    /**
     * 任务类型
     * *非空 varchar(20)*
     */
    private VolunteerTaskType taskType;

    /**
     * 任务来源 id
     * *bigint*
     */
    private Long taskSourceId;

    /**
     * 任务标题
     * *非空 varchar(255)*
     */
    private String title;

    /**
     * 任务内容
     * *text*
     */
    private String content;

    /**
     * 服务地点
     * *varchar(255)*
     */
    private String serviceAddress;

    /**
     * 省份
     * *varchar(50)*
     */
    private String province;

    /**
     * 城市
     * *varchar(50)*
     */
    private String city;

    /**
     * 区县
     * *varchar(50)*
     */
    private String district;

    /**
     * 开始时间
     * *非空 datetime*
     */
    private Date startTime;

    /**
     * 结束时间
     * *非空 datetime*
     */
    private Date endTime;

    /**
     * 预计服务时长
     * *decimal(10,2)*
     */
    private BigDecimal estimatedHours;

    /**
     * 排班状态
     * *非空 varchar(20)*
     */
    private VolunteerShiftStatus status;

    /**
     * 备注
     * *text*
     */
    private String remark;

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
}
