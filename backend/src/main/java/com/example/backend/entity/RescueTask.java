package com.example.backend.entity;

import com.example.backend.entity.property.RescueTaskStatus;
import com.example.backend.entity.property.RescueTaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 救助任务
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescueTask implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 上一个任务 id
     * *外键:rescueTask(id) long*
     */
    private Long previousId;

    /**
     * 创建者
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 审核者
     * *外键:user(id) 非空 bigint*
     */
    private Long approveId;

    /**
     * 简述
     * *非空 varchar(20)*
     */
    private String summary;

    /**
     * 详细描述
     * *text*
     */
    private String description;

    /**
     * 状态
     * *非空 varchar(20)*
     */
    private RescueTaskStatus status;

    /**
     * 类型
     * *非空 varchar(20)*
     */
    private RescueTaskType type;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 最后一次更新时间
     * *非空 datetime*
     */
    private Date updateTime;
}
