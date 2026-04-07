package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 协议文件（纸质扫描件 / 签名）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreementFile implements IId, IFile {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 协议 id
     * *外键:agreement(id) bigint*
     */
    private Long agreementId;

    /**
     * 文件名
     * *非空 varchar(20)*
     */
    private String filename;

    /**
     * 页数，0 表示签名
     * *非空 int*
     */
    private Integer page;

    /**
     * 上传时间
     * *非空 datetime*
     */
    private Date createTime;
}
