package com.example.backend.entity;

import com.example.backend.entity.property.AdoptFollowStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 领养跟踪任务
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowTask implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 申请 id
     * *外键:adopt(id) 非空 int*
     */
    private Long adoptId;

    /**
     * 安排人员
     * *外键:user(id) 非空 int*
     */
    private Long workerId;

    /**
     * 志愿者 id
     * *外键:user(id) int*
     */
    private Long volunteerId;

    /**
     * 上次访问时间
     * *datetime*
     */
    private Date lastTime;

    /**
     * 计划访问时间
     * *非空 datetime*
     */
    private Date planTime;

    /**
     * 状态
     * *非空 varchar(20)*
     */
    private AdoptFollowStatus status;

    /**
     * 备注（如节假日顺延等）
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

