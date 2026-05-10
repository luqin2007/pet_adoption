package com.example.backend.entity;

import com.example.backend.entity.property.VolunteerShiftStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 志愿者排班状态变更记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerShiftStatusRecord implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 排班 id
     * *外键:volunteerShift(id) bigint*
     */
    private Long shiftId;

    /**
     * 旧状态
     * *非空 varchar(20)*
     */
    private VolunteerShiftStatus statusFrom;

    /**
     * 新状态Z
     * *非空 varchar(20)*
     */
    private VolunteerShiftStatus statusTo;

    /**
     * 状态变更说明
     * *varchar(255)*
     */
    private String comment;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
