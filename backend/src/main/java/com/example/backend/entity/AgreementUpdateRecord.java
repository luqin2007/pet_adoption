package com.example.backend.entity;

import com.example.backend.entity.property.AgreementUpdateType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 协议更新记录
 */
@Data
@NoArgsConstructor
public class AgreementUpdateRecord implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 协议 id
     * *外键:agreement(id) 非空 bigint*
     */
    private Long agreementId;

    /**
     * 原始协议内容
     * *非空 text*
     */
    private String content;

    /**
     * 更新类型
     * *非空 varchar(20)*
     */
    private AgreementUpdateType type;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    public AgreementUpdateRecord(Agreement agreement, AgreementUpdateType type) {
        this.id = null;
        this.agreementId = agreement.getId();
        this.content = agreement.getContent();
        this.type = type;
        this.createTime = new Date();
    }
}
