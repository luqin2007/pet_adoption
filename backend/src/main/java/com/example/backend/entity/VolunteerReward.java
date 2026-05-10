package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.property.VolunteerRewardStatus;
import com.example.backend.entity.property.VolunteerRewardType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿者激励记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerReward implements IId {

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
     * 统计开始时间
     * *非空 datetime*
     */
    private Date periodStart;

    /**
     * 统计结束时间
     * *非空 datetime*
     */
    private Date periodEnd;

    /**
     * 服务次数
     * *int*
     */
    private Integer serviceCount;

    /**
     * 累计服务时长
     * *decimal(10,2)*
     */
    private BigDecimal totalHours;

    /**
     * 激励类型
     * *非空 varchar(20)*
     */
    private VolunteerRewardType rewardType;

    /**
     * 激励内容
     * *varchar(255)*
     */
    private String rewardValue;

    /**
     * 激励原因
     * *text*
     */
    private String rewardReason;

    /**
     * 激励状态
     * *非空 varchar(20)*
     */
    private VolunteerRewardStatus status;

    /**
     * 发放人 id
     * *外键:user(id) bigint*
     */
    private Long issuerId;

    /**
     * 发放时间
     * *datetime*
     */
    private Date issueTime;

    /**
     * 备注
     * *非数据库字段，兼容接口展示*
     */
    @TableField(exist = false)
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
}
