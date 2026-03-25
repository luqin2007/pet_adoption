package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 过敏史
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllergyHistory implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 初诊登记 id
     * *外键:firstVisitRegistration(id) 非空 long*
     */
    private Long registrationId;

    /**
     * 过敏源
     * *非空 varchar(255)*
     */
    private String source;

    /**
     * 发现时间
     * *非空 datetime*
     */
    private Date discoveryTime;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
