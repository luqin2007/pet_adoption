package com.example.backend.entity;

import com.example.backend.util.FileUtils;
import lombok.Data;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 媒体信息（图片、视频）
 */
@Data
public class MediaInfo implements IId {

    /**
     * *主键 long*
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
    private String parentType;

    /**
     * 上传用户 id
     * *外键:user(id) 非空 long*
     */
    private Long userId;

    /**
     * 资源名
     * *非空 varchar(255)*
     */
    private String name;

    /**
     * 资源介绍
     * *可空 text*
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
     * *非空 tinyint*
     */
    private Integer type;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 从上传文件生成媒体信息
     *
     * @param parentId   与之关联的资源 id
     * @param parentType 关联类型
     * @param userId     上传用户 id
     * @param file       上传文件
     */
    public static MediaInfo fromUpload(Long parentId, String parentType, Long userId, MultipartFile file) {
        String oriName = file.getOriginalFilename();
        String name = FileUtils.getNameWithoutExtension(oriName);
        Date now = new Date();
        Pair<String, Integer> pair = FileUtils.getFileExtensionAndType(file);

        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.setParentId(parentId);
        mediaInfo.setParentType(parentType);
        mediaInfo.setUserId(userId);
        mediaInfo.setName(name);
        mediaInfo.setDescription(null);
        mediaInfo.setIsCover(false);
        mediaInfo.setFilename(FileUtils.generateFilename(name, now, pair.getFirst()));
        mediaInfo.setType(pair.getSecond());
        mediaInfo.setCreateTime(now);
        return mediaInfo;
    }
}
