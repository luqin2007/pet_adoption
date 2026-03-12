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
    private Long id;

    /**
     * 救助任务 id
     * *外键:rescueTask(id) 非空 long*
     */
    private Long taskId;

    /**
     * 发起者 id
     * *外键:user(id) 非空 long*
     */
    private Long userId;

    /**
     * 状态审核者 id
     * *外键:user(id) 非空 long*
     */
    private Long approveId;

    /**
     * 记录类型
     * *非空 tinyint*
     */
    private Integer action;

    /**
     * 修改前的任务状态
     * *非空 tinyint*
     */
    private Integer statusFrom;

    /**
     * 修改后的任务状态
     * *非空 tinyint*
     */
    private Integer statusTo;

    /**
     * 修改原因
     * *非空 text*
     */
    private String reason;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

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
