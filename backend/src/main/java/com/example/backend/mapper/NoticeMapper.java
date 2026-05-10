package com.example.backend.mapper;

import com.example.backend.dto.NoticeQueryParams;
import com.example.backend.entity.Notice;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.Set;

/**
 * 索引：
 * - (userId, important, read, source)
 */
@Mapper
public interface NoticeMapper extends IBaseMapper<Notice> {

    default MPLambdaQuery<Notice> queryByUser(Long userId, NoticeQueryParams params) {
        return lambdaQuery()
                .eq(Notice::getReceiverId, userId)
                .eq(Notice::getRead, params.getRead())
                .in(Notice::getSource, NoticeSource::get, params.getSource())
                .in(Notice::getCreateTime, params.getTime0(), params.getTime1())
                .desc(Notice::getCreateTime);
    }

    default MPLambdaQuery<Notice> queryUnread(Long userId) {
        return lambdaQuery()
                .eq(Notice::getReceiverId, userId)
                .eq(Notice::getRead, false);
    }

    default MPLambdaUpdate<Notice> updateRead(Set<Long> noticeIds, Long userId, Boolean isRead) {
        Date now = new Date();
        isRead = Boolean.TRUE.equals(isRead);
        return lambdaUpdate()
                .in(Notice::getId, noticeIds)
                .eq(Notice::getReceiverId, userId)
                .set(Notice::getRead, isRead)
                .set(Notice::getReadTime, isRead ? now : null)
                .set(Notice::getUpdateTime, now);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.notice";
    }

    @Override
    default Class<Notice> getEntityClass() {
        return Notice.class;
    }
}
