package com.example.backend.dto;

import com.example.backend.entity.DonationStatusUpdateRecord;
import com.example.backend.entity.User;
import com.example.backend.entity.property.DonationStatus;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.USER;

@Data
@AllArgsConstructor
public class DonationStatusUpdateResponse implements IResponse {

    private Long id;
    private Long donationId;
    private DonationStatus oldStatus;
    private DonationStatus newStatus;
    private String reason;
    private Date createTime;

    // user
    private Long userId;
    private String username;
    private String avatar;

    /**
     * User: id, username, avatar
     */
    public static DonationStatusUpdateResponse create(DonationStatusUpdateRecord record, User user) {
        return new DonationStatusUpdateResponse(
                record.getId(),
                record.getDonationId(),
                record.getOldStatus(),
                record.getNewStatus(),
                record.getReason(),
                record.getCreateTime(),
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(USER, user.getId(), user.getAvatar()));
    }

    /**
     * User: id, username, avatar<br>
     * <br>
     * users: DonationStatusUpdateRecord.userId
     */
    public static DonationStatusUpdateResponse createBatch(DonationStatusUpdateRecord record, Map<Long, User> users) {
        return create(record, users.get(record.getUserId()));
    }
}
