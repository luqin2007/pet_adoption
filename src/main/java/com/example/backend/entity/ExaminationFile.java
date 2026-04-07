package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 检查文件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationFile implements IId, IFile {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 检验 id
     * *外键:examination(id) 非空 bigint*
     */
    private Long examinationId;

    /**
     * 名称
     * *非空 varchar(255)*
     */
    private String name;

    /**
     * 文件名
     * *非空 varchar(255)*
     */
    private String filename;

    /**
     * 文件名
     * *非空 datetime*
     */
    private Date createTime;
}
