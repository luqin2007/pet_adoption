package com.example.backend.entity;

import com.example.backend.entity.property.ParentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 领养/寄养协议
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agreement implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 申请 id
     * *非空 int*
     */
    private Long parentId;

    /**
     * 申请类型 (ADOPT / BREADING)
     * *非空 varchar(20)*
     */
    private ParentType parentType;

    /**
     * 电子协议正文，null 表示纸质协议扫描
     * *text*
     */
    private String content;

    /**
     * 签名图片
     * *varchar(20)*
     */
    private String sign;

    /**
     * 签署时间
     * *datetime*
     */
    private Date signTime;

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

