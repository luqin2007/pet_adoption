package com.example.backend.dto;

import com.example.backend.entity.Breading;
import com.example.backend.entity.User;
import com.example.backend.entity.property.AdoptBreadingStatus;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.USER;

@Data
@AllArgsConstructor
public class BreadingResponse implements IResponse {

    private Long id;
    private String petName;
    private Integer petAge;
    private String petType;
    private String petBreed;
    private String petDescription;
    private AdoptBreadingStatus status;
    private String requirement;
    private String rejectReason;
    private Date reviewTime;
    private Date startTime;
    private Date endTime;
    private Date createTime;
    private Date updateTime;

    // user
    private Long applicantId;
    private String applicantName;
    private String applicantAvatar;
    private String applicantPhone;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerAvatar;

    /**
     * User: id, username, avatar
     */
    public static BreadingResponse create(Breading breading, User applicant, User reviewer) {
        return new BreadingResponse(
                breading.getId(),
                breading.getPetName(),
                breading.getPetAge(),
                breading.getPetType(),
                breading.getPetBreed(),
                breading.getPetDescription(),
                breading.getStatus(),
                breading.getRequirement(),
                breading.getRejectReason(),
                breading.getReviewTime(),
                breading.getStartTime(),
                breading.getEndTime(),
                breading.getCreateTime(),
                breading.getUpdateTime(),
                breading.getApplicantId(),
                applicant.getUsername(),
                FileUtils.generateAssetUrl(USER, applicant.getId(), applicant.getAvatar()),
                breading.getApplicantPhone(),
                breading.getReviewerId(),
                reviewer == null ? null : reviewer.getUsername(),
                reviewer == null ? null : FileUtils.generateAssetUrl(USER, reviewer.getId(), reviewer.getAvatar()));
    }

    /**
     * User: id, username, avatar<br>
     * <br>
     * users: Breading.applicantId, Breading.reviewerId
     */
    public static BreadingResponse createBatch(Breading breading, Map<Long, User> users) {
        return create(breading, users.get(breading.getApplicantId()), users.get(breading.getReviewerId()));
    }
}
