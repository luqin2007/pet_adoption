package com.example.backend.entity;

import com.example.backend.entity.property.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationFile implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 捐赠 id
     * *外键:donation(id) 非空 bigint*
     */
    private Long donationId;

    /**
     * 文件类型
     * *非空 varchar(20)*
     */
    private MediaType type;

    /**
     * 文件名
     * *非空 varchar(255)*
     */
    private String filename;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
