package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.property.VolunteerShiftStatus;
import com.example.backend.entity.property.VolunteerTaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.math.BigDecimal;

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
     * 任务 id
     * *bigint*
     */
    private Long taskId;

    /**
     * 任务类型
     * *非数据库字段，存储于 volunteer_task 表*
     */
    @TableField(exist = false)
    private VolunteerTaskType taskType;

    /**
     * 任务标题
     * *非数据库字段，用于接口展示*
     */
    @TableField(exist = false)
    private String title;

    /**
     * 任务内容
     * *非数据库字段，存储于 volunteer_task 表*
     */
    @TableField(exist = false)
    private String content;

    /**
     * 服务地点
     * *非数据库字段，存储于 location 表*
     */
    @TableField(exist = false)
    private String serviceAddress;

    /**
     * 省份
     * *非数据库字段，存储于 location 表*
     */
    @TableField(exist = false)
    private String province;

    /**
     * 城市
     * *非数据库字段，存储于 location 表*
     */
    @TableField(exist = false)
    private String city;

    /**
     * 区县
     * *非数据库字段，存储于 location 表*
     */
    @TableField(exist = false)
    private String district;

    /**
     * 开始时间
     * *非数据库字段，存储于 volunteer_task 表*
     */
    @TableField(exist = false)
    private Date startTime;

    /**
     * 结束时间
     * *非数据库字段，存储于 volunteer_task 表*
     */
    @TableField(exist = false)
    private Date endTime;

    /**
     * 预计服务时长
     * *非数据库字段，用于接口展示*
     */
    @TableField(exist = false)
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
     * 修改时间
     * *非空 datetime*
     */
    private Date updateTime;

    public VolunteerShift(Long id,
                          Long volunteerId,
                          Long assignerId,
                          VolunteerTaskType taskType,
                          Long taskId,
                          String title,
                          String content,
                          String serviceAddress,
                          String province,
                          String city,
                          String district,
                          Date startTime,
                          Date endTime,
                          BigDecimal estimatedHours,
                          VolunteerShiftStatus status,
                          String remark,
                          Date createTime,
                          Date updateTime) {
        this.id = id;
        this.volunteerId = volunteerId;
        this.assignerId = assignerId;
        this.taskId = taskId;
        this.taskType = taskType;
        this.title = title;
        this.content = content;
        this.serviceAddress = serviceAddress;
        this.province = province;
        this.city = city;
        this.district = district;
        this.startTime = startTime;
        this.endTime = endTime;
        this.estimatedHours = estimatedHours;
        this.status = status;
        this.remark = remark;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}
