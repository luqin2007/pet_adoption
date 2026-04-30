package com.example.backend.dto;

import com.example.backend.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class NoticeResponse implements IResponse {

    private Long id;
    private Long receiverId;
    private String source;
    private String title;
    private String content;
    private Boolean read;
    private Date readTime;
    private Date createTime;

    public static NoticeResponse create(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getReceiverId(),
                notice.getSource().name(),
                notice.getTitle(),
                notice.getContent(),
                notice.getRead(),
                notice.getReadTime(),
                notice.getCreateTime());
    }
}
