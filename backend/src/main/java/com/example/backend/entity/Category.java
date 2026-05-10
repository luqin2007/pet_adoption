package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 物资分类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 分类名称
     * *非空 varchar(255)*
     */
    private String name;

    /**
     * 分类说明
     * *text*
     */
    private String description;

    /**
     * 是否废弃
     * *非空 boolean*
     */
    private Boolean isDiscard;

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
