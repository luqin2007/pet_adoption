package com.example.backend.dto;

import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.property.VolunteerTaskType;
import lombok.Data;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿者排班更新请求
 */
@Data
public class VolunteerShiftUpdateRequest implements IRequest, IValidatedRequest {

    private Long volunteerId;
    private String taskType;
    private Long taskSourceId;
    private String title;
    private String content;
    private String serviceAddress;
    private String province;
    private String city;
    private String district;
    private Date startTime;
    private Date endTime;
    private BigDecimal estimatedHours;
    private String remark;

    /**
     * 将请求内容应用到排班实体
     */
    public void applyTo(VolunteerShift shift) {
        if (volunteerId != null) shift.setVolunteerId(volunteerId);
        if (taskType != null) shift.setTaskType(VolunteerTaskType.get(taskType));
        if (taskSourceId != null) shift.setTaskSourceId(taskSourceId);
        if (title != null) shift.setTitle(title);
        if (content != null) shift.setContent(content);
        if (serviceAddress != null) shift.setServiceAddress(serviceAddress);
        if (province != null) shift.setProvince(province);
        if (city != null) shift.setCity(city);
        if (district != null) shift.setDistrict(district);
        if (startTime != null) shift.setStartTime(startTime);
        if (endTime != null) shift.setEndTime(endTime);
        if (estimatedHours != null) shift.setEstimatedHours(estimatedHours);
        if (remark != null) shift.setRemark(remark);
        shift.setUpdateTime(new Date());
    }

    /**
     * 校验任务类型和时间范围
     */
    @Override
    public void validate(Errors errors) {
        validateEnum(errors, VolunteerShiftUpdateRequest::getTaskType, VolunteerTaskType.class, "request.volunteer.shift.type");
        validateTime(errors, VolunteerShiftUpdateRequest::getStartTime, VolunteerShiftUpdateRequest::getEndTime);
    }
}
