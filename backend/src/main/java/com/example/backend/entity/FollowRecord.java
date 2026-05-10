package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 领养跟踪记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowRecord implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 跟踪任务 id
     * *外键:followTask(id) 非空 bigint*
     */
    private Long taskId;

    /**
     * 志愿者 id
     * *外键:user(id) 非空 bigint*
     */
    private Long volunteerId;

    /**
     * 简介状况
     * *非空 varchar(255)*
     */
    private String summary;

    /**
     * 回访时间
     * *非空 datetime*
     */
    private Date visitTime;

    /**
     * 生活状态
     * *text*
     */
    private String lifeStatus;

    /**
     * 健康状态
     * *text*
     */
    private String healthStatus;

    /**
     * 虐待/弃养风险观察
     * *text*
     */
    private String risk;

    /**
     * 建议与反馈
     * *text*
     */
    private String suggestion;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}

