package com.example.backend.dto;

import com.example.backend.entity.Order;
import com.example.backend.entity.RehabPlan;
import com.example.backend.entity.RehabPlanStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

import static com.example.backend.entity.property.ParentType.REHAB_PLAN;
import static com.example.backend.entity.property.RehabPlanStatusProp.ACTIVE;

@Data
public class RehabPlanAddRequest implements IRequest {

    @NotBlank(message = "request.pet.age")
    private Integer age;

    @NotBlank(message = "request.medical.rehab.task.title")
    private String title;

    @NotBlank(message = "request.medical.rehab.task.content")
    private String content;

    @NotBlank(message = "request.medical.rehab.task.frequency")
    private String frequency;

    @NotNull(message = "request.medical.exam.text_type")
    private String type;

    @NotNull(message = "request.start_time")
    private Date startTime;

    @NotNull(message = "request.medical.rehab.task.end_time")
    private Date endTime;

    private List<OrderRequest> orders;

    public RehabPlan create(Long doctorId, Long petId) {
        return new RehabPlan(null,
                doctorId,
                petId,
                age,
                title,
                content,
                frequency,
                ACTIVE,
                startTime,
                endTime,
                new Date());
    }

    public List<Order> createOrders(Long allowerId, Long planId) {
        return orders.stream()
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
