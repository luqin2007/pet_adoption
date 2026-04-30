package com.example.backend.dto;

import com.example.backend.entity.*;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.property.VolunteerRecordStatus;
import com.example.backend.entity.property.VolunteerShiftStatus;
import com.example.backend.entity.property.VolunteerTaskType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 志愿者排班响应
 */
@Data
@AllArgsConstructor
public class VolunteerShiftResponse implements IResponse {

    /**
     * 排班 id
     */
    private Long id;
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
     * 排班人 id
     */
    private Long assignerId;
    /**
     * 排班人名称
     */
    private String assignerName;
    /**
     * 排班人头像
     */
    private String assignerAvatar;
    /**
     * 任务类型
     */
    private VolunteerTaskType taskType;
    /**
     * 任务来源 id
     */
    private Long taskSourceId;
    /**
     * 任务标题
     */
    private String title;
    /**
     * 任务内容
     */
    private String content;
    /**
     * 服务地点
     */
    private String serviceAddress;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 区县
     */
    private String district;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 排班状态
     */
    private VolunteerShiftStatus status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 服务记录 id
     */
    private Long recordId;
    /**
     * 服务记录状态
     */
    private VolunteerRecordStatus recordStatus;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 状态更新记录
     */
    private List<VolunteerShiftStatusRecord> statusRecords;

    public static VolunteerShiftResponse create(VolunteerShift shift,
                                                VolunteerTask task, Location location,
                                                User volunteer, User assigner,
                                                VolunteerServiceRecord record,
                                                List<VolunteerShiftStatusRecord> statusRecords) {
        return new VolunteerShiftResponse(
                shift.getId(),
                shift.getVolunteerId(),
                volunteer == null ? null : volunteer.getUsername(),
                volunteer == null ? null : FileUtils.generateAssetUrl(ParentType.USER, volunteer.getId(), volunteer.getAvatar()),
                shift.getAssignerId(),
                assigner == null ? null : assigner.getUsername(),
                assigner == null ? null : FileUtils.generateAssetUrl(ParentType.USER, assigner.getId(), assigner.getAvatar()),
                task.getTaskType(),
                shift.getTaskId(),
                task.getTitle(),
                task.getContent(),
                location.getDetailAddress(),
                location.getProvince(),
                location.getCity(),
                location.getDistrict(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getStatus(),
                shift.getRemark(),
                record == null ? null : record.getId(),
                record == null ? null : record.getStatus(),
                shift.getCreateTime(),
                shift.getUpdateTime(),
                statusRecords);
    }

    /**
     * 批量构造响应
     */
    public static VolunteerShiftResponse createBatch(VolunteerShift shift,
                                                     Map<Long, VolunteerTask> tasks,
                                                     Map<Long, Location> locations,
                                                     Map<Long, User> users,
                                                     Map<Long, VolunteerServiceRecord> records,
                                                     Map<Long, List<VolunteerShiftStatusRecord>> statusRecords) {
        return create(shift,
                tasks.get(shift.getTaskId()),
                locations.get(shift.getTaskId()),
                users.get(shift.getVolunteerId()),
                users.get(shift.getAssignerId()),
                records.get(shift.getId()),
                statusRecords.get(shift.getId()));
    }
}
