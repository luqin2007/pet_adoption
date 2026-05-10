package com.example.backend.entity;

import com.example.backend.entity.property.ParentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 位置信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 绑定类型 id
     * *非空 bigint*
     */
    private Long parentId;

    /**
     * 绑定类型
     * *非空 varchar(20)*
     */
    private ParentType parentType;

    /**
     * 发现/记录者 id
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 省份
     * *非空 varchar(20)*
     */
    private String province;

    /**
     * 城市
     * *非空 varchar(20)*
     */
    private String city;

    /**
     * 县/县级市
     * *非空 varchar(20)*
     */
    private String district;

    /**
     * 详细地址
     * *非空 varchar(255)*
     */
    private String detailAddress;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
