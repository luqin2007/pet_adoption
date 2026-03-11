package com.example.backend.entity;

import lombok.Data;

import java.util.Date;

@Data
public class RescueTaskSuggestion {

    public static final Integer STATUS_CREATED = 0;
    public static final Integer STATUS_APPROVED = 1;
    public static final Integer STATUS_PROCESSING = 2;
    public static final Integer STATUS_COMPLETED_NEXT = 3;
    public static final Integer STATUS_COMPLETED = 4;
    public static final Integer STATUS_DISCARDED = 5;

    private Long id;

    /**
     * 上一个任务 id
     */
    private Long previousId;

    /**
     * 上一个版本 id
     */
    private Long previousVersion;

    /**
     * 创建者
     */
    private Long createId;

    /**
     * 审核者
     */
    private Long approveId;

    /**
     * 简述
     */
    private String summary;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 状态
     * 0 - 已创建
     * 1 - 审核通过，之后无法修改，只能附加建议
     * 2 - 正在处理中
     * 3 - 处理完成，但还需下一步处理
     * 4 - 任务完成
     * 5 - 已废弃
     */
    private Integer status;

    /**
     * 类型
     * 0 - 发现流浪宠物
     * 1 - 医疗救助
     * 2 - 其他
     */
    private Integer type;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后一次更新时间
     */
    private Date updateTime;
}
