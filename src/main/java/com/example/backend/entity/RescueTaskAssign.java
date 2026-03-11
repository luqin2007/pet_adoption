package com.example.backend.entity;

import lombok.Data;

import java.sql.Date;

/**
 * 救助任务分配信息
 */
@Data
public class RescueTaskAssign implements IId {

    /**
     * *主键 long*
     */
    private Long id;

    /**
     * 救助任务 id
     * *外键:rescueTask(id) 非空 long*
     */
    private Long taskId;

    /**
     * 任务执行者 id
     * *外键:user(id) 非空 long*
     */
    private Long userId;

    /**
     * 任务分配者 id
     * *外键:user(id) 非空 long*
     */
    private Long assignerId;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    public static RescueTaskAssign create(Long taskId, Long userId, Long assignerId) {
        RescueTaskAssign rescueTaskAssign = new RescueTaskAssign();
        rescueTaskAssign.setTaskId(taskId);
        rescueTaskAssign.setUserId(userId);
        rescueTaskAssign.setAssignerId(assignerId);
        rescueTaskAssign.setCreateTime(new Date(System.currentTimeMillis()));
        return rescueTaskAssign;
    }
}
