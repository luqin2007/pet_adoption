package com.example.backend.entity;

import com.example.backend.entity.property.VolunteerTaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 志愿者任务
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerTask implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 任务类型
     * *非空 varchar(20)*
     */
    private VolunteerTaskType taskType;

    /**
     * 任务 id
     * *bigint*
     */
    private Long taskId;

    /**
     * 任务地址 id
     * *外键:volunteerLocation(id) 非空 bigint*
     */
    private Long locationId;

    /**
     * 任务标题
     * *非空 varchar(255)*
     */
    private String title;

    /**
     * 任务内容
     * *text*
     */
    private String content;

    /**
     * 开始时间
     * *非空 datetime*
     */
    private Date startTime;

    /**
     * 结束时间
     * *非空 datetime*
     */
    private Date endTime;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
