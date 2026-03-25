package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 免疫史
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImmunityHistory implements IId {

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
     * 免疫药品
     * *非空 varchar(255)*
     */
    private String medicine;

    /**
     * 第几次免疫
     * *非空 integer*
     */
    private Integer count;

    /**
     * 总需要的免疫次数
     * *非空 integer*
     */
    private Integer total;

    /**
     * 免疫时间
     * *非空 datetime*
     */
    private Date immunityTime;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
