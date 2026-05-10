package com.example.backend.dto;

import com.example.backend.entity.LostPet;
import com.example.backend.entity.LostPetClaim;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ClaimStatus;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.USER;

/**
 * 认领申请响应
 */
@Data
@AllArgsConstructor
public class LostPetClaimResponse implements IResponse {

    private Long id;
    private ClaimStatus status;
    private String reason;
    private String rejectReason;
    private Date reviewTime;
    private Date claimTime;
    private Date createTime;
    private Date updateTime;

    // lost pet
    private Long lostPetId;
    private Long petId;
    private String lostPetName;
    private String lostPetType;
    private String lostPetBreed;

    // applicant
    private Long applicantId;
    private String applicantName;
    private String applicantPhone;
    private String applicantAvatar;

    // reviewer
    private Long reviewerId;
    private String reviewerName;
    private String reviewerAvatar;

    /**
     * LostPet: id, name, type, breed<br>
     * User: id, username, avatar
     */
    public static LostPetClaimResponse create(LostPetClaim claim,
                                              LostPet lostPet,
                                              User applicant, User reviewer) {
        return new LostPetClaimResponse(
                claim.getId(),
                claim.getStatus(),
                claim.getReason(),
                claim.getApproveReason(),
                claim.getReviewTime(),
                claim.getClaimTime(),
                claim.getCreateTime(),
                claim.getUpdateTime(),
                claim.getLostPetId(),
                lostPet != null ? lostPet.getPetId() : null,
                lostPet != null ? lostPet.getName() : null,
                lostPet != null ? lostPet.getType() : null,
                lostPet != null ? lostPet.getBreed() : null,
                claim.getApplicantId(),
                applicant.getUsername(),
                claim.getApplicantPhone(),
                FileUtils.generateAssetUrl(USER, applicant.getId(), applicant.getAvatar()),
                claim.getReviewerId(),
                reviewer != null ? reviewer.getUsername() : null,
                reviewer != null ? FileUtils.generateAssetUrl(USER, reviewer.getId(), reviewer.getAvatar()) : null);
    }

    /**
     * LostPet: id, name, type, breed<br>
     * User: id, username, avatar<br>
     * <br>
     * lostPets: claim.lostPetId<br>
     * users: claim.applicantId, claim.reviewerId
     */
    public static LostPetClaimResponse createBatch(LostPetClaim claim, Map<Long, LostPet> lostPets, Map<Long, User> users) {
        return create(claim,
                lostPets.get(claim.getLostPetId()),
                users.get(claim.getApplicantId()),
                users.get(claim.getReviewerId()));
    }
}
