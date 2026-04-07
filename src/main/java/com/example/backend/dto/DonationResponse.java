package com.example.backend.dto;

import com.example.backend.entity.Donation;
import com.example.backend.entity.User;
import com.example.backend.entity.property.DeliveryType;
import com.example.backend.entity.property.DonationStatus;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.USER;

@Data
@AllArgsConstructor
public class DonationResponse implements IResponse {

    private Long id;
    private DeliveryType delivery;
    private String address;
    private String trackingNumber;
    private String description;
    private DonationStatus status;
    private Date createTime;
    private Date updateTime;
    private List<DonationFileResponse> files;
    private List<DonationItemResponse> items;
    private List<DonationStatusUpdateResponse> records;

    // user
    private Long userId;
    private String username;
    private String avatar;

    /**
     * User: id, username, avatar
     */
    public static DonationResponse create(Donation donation, User user,
                                          List<DonationFileResponse> files,
                                          List<DonationItemResponse> items,
                                          List<DonationStatusUpdateResponse> records) {
        return new DonationResponse(donation.getId(),
                donation.getDelivery(),
                donation.getAddress(),
                donation.getTrackingNumber(),
                donation.getDescription(),
                donation.getStatus(),
                donation.getCreateTime(),
                donation.getUpdateTime(),
                files,
                items,
                records,
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(USER, user.getId(), user.getAvatar()));
    }

    /**
     * User: id, username, avatar<br>
     * <br>
     * users: Donation.userId<br>
     * files: Donation.id<br>
     * items: Donation.id<br>
     * records: Donation.id
     */
    public static DonationResponse createBatch(Donation donation, Map<Long, User> users,
                                               Map<Long, List<DonationFileResponse>> files,
                                               Map<Long, List<DonationItemResponse>> items,
                                               Map<Long, List<DonationStatusUpdateResponse>> records) {
        return create(donation,
                users.get(donation.getUserId()),
                files.get(donation.getId()),
                items.get(donation.getId()),
                records.get(donation.getId()));
    }
}
