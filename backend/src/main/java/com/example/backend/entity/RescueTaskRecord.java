package com.example.backend.entity;

import com.example.backend.entity.property.RescueTaskAction;
import com.example.backend.entity.property.RescueTaskStatus;
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
     * *主键 bigint*
     */
    private Long id;

    /**
     * 救助任务 id
     * *外键:rescueTask(id) 非空 bigint*
     */
    private Long taskId;

    /**
     * 发起者 id
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 记录类型
     * *非空 varchar(20)*
     */
    private RescueTaskAction action;

    /**
     * 修改前的任务状态
     * *非空 varchar(20)*
     */
    private RescueTaskStatus statusFrom;

    /**
     * 修改后的任务状态
     * *非空 varchar(20)*
     */
    private RescueTaskStatus statusTo;

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
}
