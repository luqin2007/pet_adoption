package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 医疗检查记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Examination implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 检查人
     * *外键:user(id) 非空 int*
     */
    private Long userId;

    /**
     * 绑定病历 id
     * *外键:medicalDetail(id) 非空 int*
     */
    private Long detailId;

    /**
     * 检查名
     * *text*
     */
    private String name;

    /**
     * 文本记录
     * *text*
     */
    private String text;

    /**
     * 文本类型
     * *text*
     */
    private Integer textType;

    /**
     * 医疗记录类型
     * *非空 tinyint*
     */
    private Integer examType;

    /**
     * 附件文件名
     * *varchar(255)*
     */
    private String filename;

    /**
     * 价格
     * *非空 decimal(10,2)*
     */
    private Double price;

    /**
     * 检查时间
     * *非空 datetime*
     */
    private Date checkTime;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
