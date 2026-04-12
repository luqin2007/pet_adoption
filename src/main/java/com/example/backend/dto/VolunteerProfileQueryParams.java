package com.example.backend.dto;

import com.example.backend.entity.property.VolunteerProfileStatus;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Set;

/**
 * 志愿者档案查询参数
 */
@Data
public class VolunteerProfileQueryParams implements IParam, IValidatedRequest {

    /**
     * 用户 id
     */
    private Long user;
    /**
     * 关键字
     */
    private String keyword;
    /**
     * 档案状态集合
     */
    private Set<String> status;

    /**
     * 校验档案状态
     */
    @Override
    public void validate(Errors errors) {
        validateEnums(errors, VolunteerProfileQueryParams::getStatus, VolunteerProfileStatus.class, "request.volunteer.profile.status");
    }
}
