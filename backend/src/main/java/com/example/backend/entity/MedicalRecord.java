package com.example.backend.entity;

import com.example.backend.entity.property.MedicalRecordType;
import com.example.backend.entity.property.MedicalRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 就诊记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 流浪宠物 id
     * *外键:pet(id) 非空 bigint*
     */
    private Long petId;

    /**
     * 流浪宠物年龄
     * *非空 int*
     */
    private Integer petAge;

    /**
     * 接诊人
     * *外键:user(id) 非空 bigint*
     */
    private Long doctorId;

    /**
     * 领养人
     * *外键:user(id) bigint*
     */
    private Long ownerId;

    /**
     * 领养人联系方式
     * *varchar(20)*
     */
    private String ownerPhone;

    /**
     * 诊疗流程
     * *非空 varchar(20)*
     */
    private MedicalRecordStatus status;

    /**
     * 诊疗类型
     * *非空 varchar(20)*
     */
    private MedicalRecordType type;

    /**
     * 开始时间, null 表示未开始
     * *datetime*
     */
    private Date startTime;

    /**
     * 结束时间
     * *datetime*
     */
    private Date endTime;

    /**
     * 预计价格
     * *非空 decimal*
     */
    private BigDecimal price;

    /**
     * 实际花费
     * *非空 decimal*
     */
    private BigDecimal cost;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 修改时间
     * *非空 datetime*
     */
    private Date updateTime;
}
