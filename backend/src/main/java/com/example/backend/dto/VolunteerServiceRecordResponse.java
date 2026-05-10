package com.example.backend.dto;

import com.example.backend.entity.*;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.property.VolunteerRecordStatus;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 志愿者服务记录响应
 */
@Data
@AllArgsConstructor
public class VolunteerServiceRecordResponse implements IResponse {

    /**
     * 服务记录 id
     */
    private Long id;
    /**
     * 排班 id
     */
    private Long shiftId;
    /**
     * 排班标题
     */
    private String shiftTitle;
    /**
     * 志愿者 id
     */
    private Long volunteerId;
    /**
     * 志愿者名称
     */
    private String volunteerName;
    /**
     * 志愿者头像
     */
    private String volunteerAvatar;
    /**
     * 服务开始时间
     */
    private Date startTime;
    /**
     * 服务结束时间
     */
    private Date endTime;
    /**
     * 实际服务时长
     */
    private BigDecimal actualHours;
    /**
     * 服务摘要
     */
    private String summary;
    /**
     * 服务内容
     */
    private String content;
    /**
     * 问题反馈
     */
    private String problem;
    /**
     * 改进建议
     */
    private String suggestion;
    /**
     * 服务记录状态
     */
    private VolunteerRecordStatus status;
    /**
     * 审核人 id
     */
    private Long reviewerId;
    /**
     * 审核人名称
     */
    private String reviewerName;
    /**
     * 审核人头像
     */
    private String reviewerAvatar;
    /**
     * 审核意见
     */
    private String reviewComment;
    /**
     * 审核时间
     */
    private Date reviewTime;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 根据实体构造响应
     */
    public static VolunteerServiceRecordResponse create(VolunteerServiceRecord record,
                                                        VolunteerTask task,
                                                        User volunteer,
                                                        User reviewer) {
        return new VolunteerServiceRecordResponse(
                record.getId(),
                record.getShiftId(),
                task == null ? null : task.getTitle(),
                record.getVolunteerId(),
                volunteer == null ? null : volunteer.getUsername(),
                volunteer == null ? null : FileUtils.generateAssetUrl(ParentType.USER, volunteer.getId(), volunteer.getAvatar()),
                record.getStartTime(),
                record.getEndTime(),
                record.getActualHours(),
                record.getSummary(),
                record.getContent(),
                record.getProblem(),
                record.getSuggestion(),
                record.getStatus(),
                record.getReviewerId(),
                reviewer == null ? null : reviewer.getUsername(),
                reviewer == null ? null : FileUtils.generateAssetUrl(ParentType.USER, reviewer.getId(), reviewer.getAvatar()),
                record.getReviewComment(),
                record.getReviewTime(),
                record.getCreateTime(),
                record.getUpdateTime());
    }

    /**
     * 批量构造响应
     */
    public static VolunteerServiceRecordResponse createBatch(VolunteerServiceRecord record,
                                                             Map<Long, VolunteerTask> shifts,
                                                             Map<Long, User> users) {
        return create(record,
                shifts.get(record.getShiftId()),
                users.get(record.getVolunteerId()),
                users.get(record.getReviewerId()));
    }
}
