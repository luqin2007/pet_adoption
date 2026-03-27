package com.example.backend.dto;

import com.example.backend.entity.RehabPlanStatus;
import com.example.backend.entity.User;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

import static com.example.backend.util.C.PARENT_USER;

@Data
@AllArgsConstructor
public class RehabPlanStatusResponse {

    private Long id;
    private Long planId;
    private Integer status;
    private String reason;
    private Date createTime;

    // user
    private Long userId;
    private String username;
    private String avatar;

    /**
     * User: id, username, avatar
     */
    public static RehabPlanStatusResponse create(RehabPlanStatus status, User user) {
        return new RehabPlanStatusResponse(
                status.getId(),
                status.getPlanId(),
                status.getStatus(),
                status.getReason(),
                status.getCreateTime(),
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(PARENT_USER, user.getId(), user.getAvatar()));
    }

    /**
     * User: id, username, avatar<br>
     * <br>
     * users: status.userId
     */
    public static RehabPlanStatusResponse createBatch(RehabPlanStatus status, Map<Long, User> users) {
        return create(status, users.get(status.getUserId()));
    }
}
