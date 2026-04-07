package com.example.backend.dto;

import com.example.backend.entity.Adopt;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.entity.property.AdoptBreadingStatus;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AdoptResponse implements IResponse {

    private Long id;
    private AdoptBreadingStatus status;
    private String requirement;
    private String rejectReason;
    private Date reviewTime;
    private Date adoptTime;
    private Date createTime;
    private Date updateTime;
    private List<FollowTaskResponse> followTasks;

    // pet
    private Long petId;
    private String petName;
    private String petCover;

    // user
    private Long applicantId;
    private String applicantName;
    private String applicantPhone;
    private String applicantAvatar;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerAvatar;

    /**
     * Pet: id, name<br>
     * User: id, username, avatar
     */
    public static AdoptResponse create(Adopt adopt,
                                       Pet pet, String petCover,
                                       User applicant, User reviewer,
                                       List<FollowTaskResponse> followTasks) {
        return new AdoptResponse(
                adopt.getId(),
                adopt.getStatus(),
                adopt.getRequirement(),
                adopt.getRejectReason(),
                adopt.getReviewTime(),
                adopt.getAdoptTime(),
                adopt.getCreateTime(),
                adopt.getUpdateTime(),
                followTasks,
                adopt.getPetId(),
                pet.getName(),
                petCover,
                adopt.getApplicantId(),
                applicant.getUsername(),
                adopt.getApplicantPhone(),
                FileUtils.generateAssetUrl(ParentType.USER, applicant.getId(), applicant.getAvatar()),
                adopt.getReviewerId(),
                reviewer == null ? null : reviewer.getUsername(),
                reviewer == null ? null : FileUtils.generateAssetUrl(ParentType.USER, reviewer.getId(), reviewer.getAvatar()));
    }

    /**
     * Pet: id, name
     * User: id, username, avatar
     * <br>
     * pets: Adopt.petId<br>
     * covers: Adopt.petId<br>
     * users: Adopt.applicantId, Adopt.reviewerId<br>
     * followTasks: Adopt.id
     */
    public static AdoptResponse createBatch(Adopt adopt,
                                            Map<Long, Pet> pets,
                                            Map<Long, String> covers,
                                            Map<Long, User> users,
                                            Map<Long, List<FollowTaskResponse>> followTasks) {
        return create(adopt,
                pets.get(adopt.getPetId()),
                covers.get(adopt.getPetId()),
                users.get(adopt.getApplicantId()),
                users.get(adopt.getReviewerId()),
                followTasks.get(adopt.getId()));
    }
}
