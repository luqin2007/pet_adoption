package com.example.backend.entity;

import com.example.backend.entity.property.FollowTaskStatus;
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
     * *主键 bigint*
     */
    private Long id;

    /**
     * 申请 id
     * *外键:adopt(id) 非空 bigint*
     */
    private Long adoptId;

    /**
     * 安排人员
     * *外键:user(id) 非空 bigint*
     */
    private Long workerId;

    /**
     * 志愿者 id
     * *外键:user(id) bigint*
     */
    private Long volunteerId;

    /**
     * 状态
     * *非空 varchar(20)*
     */
    private FollowTaskStatus status;

    /**
     * 备注（如节假日顺延等）
     * *text*
     */
    private String remark;

    /**
     * 计划访问时间
     * *非空 datetime*
     */
    private Date planTime;

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

