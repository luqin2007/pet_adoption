package com.example.backend.entity;

import com.example.backend.entity.property.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 走失宠物认领申请 / 记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostPetClaim implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 走失宠物报备 id
     * *外键:lostPet(id) 非空 bigint*
     */
    private Long lostPetId;

    /**
     * 找到的流浪宠物 id
     * *外键:pet(id) bigint*
     */
    private Long petId;

    /**
     * 认领申请人 id
     * *外键:user(id) 非空 bigint*
     */
    private Long applicantId;

    /**
     * 申请人联系方式
     * *非空 varchar(50)*
     */
    private String applicantPhone;

    /**
     * 审核人（救助站工作人员）id
     * *外键:user(id) bigint*
     */
    private Long reviewerId;

    /**
     * 状态
     * *非空 varchar(20)*
     */
    private ClaimStatus status;

    /**
     * 认领理由/证明材料描述
     * *非空 text*
     */
    private String reason;

    /**
     * 审核拒绝原因
     * *text*
     */
    private String approveReason;

    /**
     * 审核时间
     * *datetime*
     */
    private Date reviewTime;

    /**
     * 认领完成时间（审核通过且归还完毕）
     * *datetime*
     */
    private Date claimTime;

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
