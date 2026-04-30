package com.example.backend.entity;

import com.example.backend.entity.property.VolunteerRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿者服务记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerServiceRecord implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 排班 id
     * *外键:volunteerShift(id) 非空 bigint*
     */
    private Long shiftId;

    /**
     * 志愿者 id
     * *外键:user(id) 非空 bigint*
     */
    private Long volunteerId;

    /**
     * 服务开始时间
     * *datetime*
     */
    private Date startTime;

    /**
     * 服务结束时间
     * *datetime*
     */
    private Date endTime;

    /**
     * 实际服务时长
     * *decimal(10,2)*
     */
    private BigDecimal actualHours;

    /**
     * 服务摘要
     * *非空 varchar(255)*
     */
    private String summary;

    /**
     * 服务内容
     * *text*
     */
    private String content;

    /**
     * 问题反馈
     * *text*
     */
    private String problem;

    /**
     * 改进建议
     * *text*
     */
    private String suggestion;

    /**
     * 记录状态
     * *非空 varchar(20)*
     */
    private VolunteerRecordStatus status;

    /**
     * 审核人 id
     * *外键:user(id) bigint*
     */
    private Long reviewerId;

    /**
     * 审核意见
     * *text*
     */
    private String reviewComment;

    /**
     * 审核时间
     * *datetime*
     */
    private Date reviewTime;

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
