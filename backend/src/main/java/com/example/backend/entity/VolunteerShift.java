package com.example.backend.entity;

import com.example.backend.entity.property.VolunteerShiftStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 任务 id
     * *bigint*
     */
    private Long taskId;

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
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 修改时间
     * *非空 datetime*
     */
    private Date updateTime;
}
