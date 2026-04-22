package com.example.backend.entity;

import com.example.backend.entity.property.AdoptBreadingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 领养申请
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Adopt implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 宠物 id
     * *外键:pet(id) 非空 bigint*
     */
    private Long petId;

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
     * 领养时间
     * *datetime*
     */
    private Date adoptTime;

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
