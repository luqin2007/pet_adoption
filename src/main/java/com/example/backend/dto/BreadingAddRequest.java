package com.example.backend.dto;

import com.example.backend.entity.Breading;
import com.example.backend.entity.property.AdoptBreadingStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class BreadingAddRequest {

    @NotBlank(message = "请输入宠物名称")
    private String petName;

    @NotNull(message = "请输入宠物年龄（月）")
    @Min(value = 0, message = "请输入宠物年龄（月）")
    private Integer petAge;

    @NotBlank(message = "请输入宠物类型")
    private String petType;

    @NotBlank(message = "请输入宠物品种")
    private String petBreed;

    private String petDescription;

    @NotBlank(message = "请输入联系电话")
    private String applicantPhone;

    private String requirement;

    @NotNull(message = "请选择开始时间")
    private Date startTime;

    @NotNull(message = "请选择结束时间")
    private Date endTime;

    public Breading create(Long applicantId) {
        Date now = new Date();
        return new Breading(null,
                petName,
                petAge,
                petType,
                petBreed,
                petDescription,
                applicantId,
                applicantPhone,
                null,
                AdoptBreadingStatus.CREATE,
                requirement,
                null,
                null,
                startTime,
                endTime,
                now,
                now);
    }
}
