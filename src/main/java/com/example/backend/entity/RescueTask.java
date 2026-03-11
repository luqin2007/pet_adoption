package com.example.backend.entity;

import lombok.Data;

import java.util.Date;

/**
 * 救助任务
 */
@Data
public class RescueTask implements IId {

    /**
     * *主键 long*
     */
    private Long id;

    /**
     * 上一个任务 id
     * *外键:rescueTask(id) 可空 long*
     */
    private Long previousId;

    /**
     * 创建者
     * *外键:user(id) 非空 long*
     */
    private Long userId;

    /**
     * 审核者
     * *外键:user(id) 非空 long*
     */
    private Long approveId;

    /**
     * 简述
     * *非空 varchar(20)*
     */
    private String summary;

    /**
     * 详细描述
     * *可空 text*
     */
    private String description;

    /**
     * 状态
     * *非空 tinyint*
     */
    private Integer status;

    /**
     * 类型
     * *非空 tinyint*
     */
    private Integer type;

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
