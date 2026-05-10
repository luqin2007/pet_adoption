package com.example.backend.entity;

import com.example.backend.entity.property.DonationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationStatusUpdateRecord implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 捐赠 id
     * *外键:donation(id) 非空 bigint*
     */
    private Long donationId;

    /**
     * 修改用户 id
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 旧状态
     * *非空 varchar(20)*
     */
    private DonationStatus oldStatus;

    /**
     * 新状态
     * *非空 varchar(20)*
     */
    private DonationStatus newStatus;

    /**
     * 修改原因
     * *非空 varchar(255)*
     */
    private String reason;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
