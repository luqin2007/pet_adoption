package com.example.backend.entity;

import com.example.backend.entity.property.MediaType;
import com.example.backend.entity.property.ParentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 媒体信息（图片、视频）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile implements IId, IFile {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 与之关联的资源 id
     * *非空 long*
     */
    private Long parentId;

    /**
     * 关联类型
     * *非空 varchar(20)*
     */
    private ParentType parentType;

    /**
     * 上传用户 id
     * *外键:user(id) 非空 int*
     */
    private Long userId;

    /**
     * 资源名
     * *非空 varchar(255)*
     */
    private String name;

    /**
     * 资源介绍
     * *text*
     */
    private String description;

    /**
     * （图片）是否为封面
     * *非空 boolean*
     */
    private Boolean isCover;

    /**
     * 存储文件名
     * *非空 varchar(255)*
     */
    private String filename;

    /**
     * 类型
     * *非空 varchar(20)*
     */
    private MediaType type;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
