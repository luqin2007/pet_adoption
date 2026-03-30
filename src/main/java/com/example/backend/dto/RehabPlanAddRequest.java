package com.example.backend.dto;

import com.example.backend.entity.Order;
import com.example.backend.entity.RehabPlan;
import com.example.backend.entity.RehabPlanStatus;
import com.example.backend.entity.property.TextType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

import static com.example.backend.entity.property.ParentType.REHAB_PLAN;
import static com.example.backend.entity.property.RehabPlanStatusProp.ACTIVE;

@Data
public class RehabPlanAddRequest {

    @NotBlank(message = "请输入宠物年龄")
    private Integer age;

    @NotBlank(message = "请输入标题")
    private String title;

    @NotBlank(message = "请输入内容")
    private String content;

    @NotBlank(message = "请输入频率")
    private String frequency;

    @NotNull(message = "无效文本类型")
    private TextType type;

    @NotNull(message = "请输入开始时间")
    private Date startTime;

    @NotNull(message = "请输入（预计）结束时间")
    private Date endTime;

    private List<OrderRequest> requests;

    public RehabPlan create(Long doctorId, Long petId) {
        return new RehabPlan(null,
                doctorId,
                petId,
                age,
                title,
                content,
                type,
                frequency,
                ACTIVE,
                startTime,
                endTime,
                new Date());
    }

    public List<Order> createOrders(Long allowerId, Long planId) {
        return requests.stream()
                .map(request -> request.create(allowerId, planId, REHAB_PLAN))
                .toList();
    }

    public RehabPlanStatus createStatus(RehabPlan plan) {
        return new RehabPlanStatus(null,
                plan.getDoctorId(),
                plan.getId(),
                plan.getStatus(),
                "~~~create~~~",
                plan.getCreateTime());
    }
}
