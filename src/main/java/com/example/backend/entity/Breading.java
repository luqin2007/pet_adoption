package com.example.backend.entity;

import com.example.backend.entity.property.AdoptBreadingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 寄养申请
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Breading implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 宠物名
     * *非空 varchar(20)*
     */
    private String petName;

    /**
     * 宠物 id
     * *非空 int*
     */
    private Integer petAge;

    /**
     * 宠物类型
     * *非空 varchar(20)*
     */
    private String petType;

    /**
     * 宠物品种
     * *非空 varchar(20)*
     */
    private String petBreed;

    /**
     * 宠物描述
     * *非空 text*
     */
    private String petDescription;

    /**
     * 申请人
     * *外键:user(id) 非空 bigint*
     */
    private Long applicantId;

    /**
     * 申请人联系方式
     * *非空 varchar(20)*
     */
    private String applicantPhone;

    /**
     * 审核人
     * *外键:user(id) bigint*
     */
    private Long reviewerId;

    /**
     * 状态
     * *非空 varchar(20)*
     */
    private AdoptBreadingStatus status;

    /**
     * 申请要求
     * *非空 text*
     */
    private String requirement;

    /**
     * 审核拒绝原因
     * *text*
     */
    private String rejectReason;

    /**
     * 审核时间
     * *datetime*
     */
    private Date reviewTime;

    /**
     * 生效时间
     * *datetime*
     */
    private Date startTime;

    /**
     * 结束时间
     * *datetime*
     */
    private Date endTime;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 更新时间
     * *非空 datetime*
     */
    private Date updateTime;
}
