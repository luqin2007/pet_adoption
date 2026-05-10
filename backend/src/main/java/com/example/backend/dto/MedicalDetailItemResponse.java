package com.example.backend.dto;

import com.example.backend.entity.MedicalDetail;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class MedicalDetailItemResponse implements IResponse {

    private Long id;
    private Long recordId;
    private Date createTime;
    private Boolean isCompleted;
    private String summary;

    // user
    private Long userId;
    private String username;
    private String avatar;

    /**
     * MedicalDetail: id, recordId, createTime, isCompleted, summary<br>
     * User: id, username, avatar
     */
    public static MedicalDetailItemResponse create(MedicalDetail medicalDetail, User user) {
        return new MedicalDetailItemResponse(
                medicalDetail.getId(),
                medicalDetail.getRecordId(),
                medicalDetail.getCreateTime(),
                medicalDetail.getIsCompleted(),
                medicalDetail.getSummary(),
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, user.getId(), user.getAvatar()));
    }

    /**
     * MedicalDetail: id, recordId, userId, createTime, isCompleted, summary<br>
     * User: id, username, avatar<br>
     * <br>
     * users: MedicalDetail.userId
     */
    public static MedicalDetailItemResponse createBatch(MedicalDetail medicalDetail, Map<Long, User> users) {
        return create(medicalDetail, users.get(medicalDetail.getDoctorId()));
    }
}
