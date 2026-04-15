package com.example.backend.entity;

import com.example.backend.entity.property.ExamType;
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
     * *主键 bigint*
     */
    private Long id;

    /**
     * 检查人
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 绑定病历 id
     * *外键:medicalDetail(id) 非空 bigint*
     */
    private Long detailId;

    /**
     * 检查名
     * *非空 varchar(20)*
     */
    private String name;

    /**
     * 文本记录
     * *text*
     */
    private String text;

    /**
     * 医疗记录类型
     * *非空 varchar(20)*
     */
    private ExamType examType;

    /**
     * 附件文件名
     * *varchar(255)*
     */
    private String filename;

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
