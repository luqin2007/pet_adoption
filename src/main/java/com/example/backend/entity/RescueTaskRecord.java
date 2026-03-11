package com.example.backend.entity;

import lombok.Data;

import java.util.Date;

/**
 * 救助任务状态记录
 */
@Data
public class RescueTaskRecord implements IId {

    /**
     * *主键 long*
     */
    Long id;

    /**
     * 救助任务 id
     * *外键:rescueTask(id) 非空 long*
     */
    Long taskId;

    /**
     * 发起者 id
     * *外键:user(id) 非空 long*
     */
    Long userId;

    /**
     * 记录类型
     * *非空 tinyint*
     */
    Integer action;

    /**
     * 修改前的任务状态
     * *非空 tinyint*
     */
    Integer statusFrom;

    /**
     * 修改后的任务状态
     * *非空 tinyint*
     */
    Integer statusTo;

    /**
     * 修改原因
     * *非空 text*
     */
    String reason;

    /**
     * 创建时间
     * *非空 datetime*
     */
    Date createTime;

    /**
     * 记录任务变更
     */
    public static RescueTaskRecord create(RescueTask task, Long userId, Integer action, Integer statusFrom, String reason) {
        RescueTaskRecord record = new RescueTaskRecord();
        record.setTaskId(task.getId());
        record.setUserId(userId);
        record.setAction(action);
        record.setStatusFrom(statusFrom);
        record.setStatusTo(task.getStatus());
        record.setReason(reason);
        record.setCreateTime(new Date(System.currentTimeMillis()));
        return record;
    }
}
