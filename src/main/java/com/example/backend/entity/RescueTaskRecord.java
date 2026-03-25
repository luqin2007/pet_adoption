package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 救助任务状态记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescueTaskRecord implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 救助任务 id
     * *外键:rescueTask(id) 非空 int*
     */
    private Long taskId;

    /**
     * 发起者 id
     * *外键:user(id) 非空 int*
     */
    private Long userId;

    /**
     * 状态审核者 id
     * *外键:user(id) 非空 int*
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
        return new RescueTaskRecord(null,
                task.getId(),
                userId,
                null,
                action,
                statusFrom,
                task.getStatus(),
                reason,
                new Date(System.currentTimeMillis()));
    }
}
