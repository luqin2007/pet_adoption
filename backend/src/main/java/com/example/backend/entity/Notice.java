package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.property.NoticeSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 站内信
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notice implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 接收用户
     * *外键:user(id) 非空 bigint*
     */
    private Long receiverId;

    /**
     * 消息来源
     * *非空 varchar(20)*
     */
    private NoticeSource source;

    /**
     * 消息标题
     * *非空 varchar(255)*
     */
    private String title;

    /**
     * 消息正文
     * *非空 text*
     */
    private String content;

    /**
     * 是否已读
     * *非空 boolean*
     */
    @TableField("`read`")
    private Boolean read;

    /**
     * 已读时间
     * *datetime*
     */
    private Date readTime;

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

    public static Notice create(Long userId, NoticeSource type, String title, String content, Boolean read) {
        Date now = new Date();
        return new Notice(null,
                userId,
                type,
                title,
                content,
                read,
                null,
                now,
                now);
    }
}
