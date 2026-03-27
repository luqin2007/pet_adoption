package com.example.backend.dto;

import com.example.backend.entity.Order;
import com.example.backend.entity.TreatmentPlan;
import com.example.backend.entity.property.ParentType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TreatmentPlanAddRequest {

    @NotEmpty(message = "请填写治疗方案")
    private String plan;

    private List<OrderRequest> orders;

    @NotNull(message = "请填写开始时间")
    private Date startTime;

    private Date endTime;

    public TreatmentPlan create(Long doctorId, Long detailId) {
        return new TreatmentPlan(null,
                doctorId,
                detailId,
                plan,
                startTime,
                endTime,
                new Date(),
                false);
    }

    public List<Order> createOrders(Long allowerId, Long planId) {
        return orders.stream()
                .map(request -> request.create(allowerId, planId, ParentType.TREATMENT_PLAN))
                .toList();
    }
}
