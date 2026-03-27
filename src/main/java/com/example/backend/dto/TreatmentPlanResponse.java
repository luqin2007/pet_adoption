package com.example.backend.dto;

import com.example.backend.entity.TreatmentPlan;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class TreatmentPlanResponse {

    private Long id;
    private String plan;
    private Date creatTime;
    private Date startTime;
    private Date endTime;
    private Boolean isDiscard;

    // user
    private Long doctorId;
    private String username;
    private String avatar;

    // order
    private List<OrderResponse> orders;

    /**
     * User: id, username, avatar
     */
    public static TreatmentPlanResponse create(TreatmentPlan treatmentPlan, User doctor, List<OrderResponse> orders) {
        return new TreatmentPlanResponse(
                treatmentPlan.getId(),
                treatmentPlan.getPlan(),
                treatmentPlan.getCreateTime(),
                treatmentPlan.getStartTime(),
                treatmentPlan.getEndTime(),
                treatmentPlan.getIsDiscard(),
                treatmentPlan.getDoctorId(),
                doctor.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, doctor.getId(), doctor.getAvatar()),
                orders);
    }

    /**
     * User: id, username, avatar<br>
     * <br>
     * users: TreatmentPlan.doctorId<br>
     * orders: TreatmentPlan.id
     */
    public static TreatmentPlanResponse createBatch(TreatmentPlan treatmentPlan,
                                                    Map<Long, User> users,
                                                    Map<Long, List<OrderResponse>> orders) {
        return create(treatmentPlan, users.get(treatmentPlan.getDoctorId()), orders.get(treatmentPlan.getId()));
    }
}
