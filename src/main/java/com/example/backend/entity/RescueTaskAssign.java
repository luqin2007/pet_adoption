package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

/**
 * 救助任务分配信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescueTaskAssign implements IId {

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
     * 任务执行者 id
     * *外键:user(id) 非空 int*
     */
    private Long userId;

    /**
     * 任务分配者 id
     * *外键:user(id) 非空 int*
     */
    private Long assignerId;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    public static RescueTaskAssign create(Long taskId, Long userId, Long assignerId) {
        return new RescueTaskAssign(null,
                taskId,
                userId,
                assignerId,
                new Date(System.currentTimeMillis()));
    }
}
