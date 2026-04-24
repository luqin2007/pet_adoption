package com.example.backend.dto;

import com.example.backend.entity.MediaFile;
import com.example.backend.entity.RehabRecord;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@AllArgsConstructor
public class RehabRecordResponse implements IResponse {

    private Long id;
    private Long planId;
    private String step;
    private String reaction;
    private String note;
    private Date createTime;

    private Long userId;
    private String username;
    private String avatar;

    private List<String> assets;

    /**
     * User: id, username, avatar<br>
     * MediaFile: filename
     */
    public static RehabRecordResponse create(RehabRecord record, User user, List<MediaFile> files) {
        return new RehabRecordResponse(
                record.getId(),
                record.getPlanId(),
                record.getStep(),
                record.getReaction(),
                record.getNote(),
                record.getCreateTime(),
                user.getId(),
                user.getUsername(),
                FileUtils.generateAssetUrl(ParentType.USER, user.getId(), user.getAvatar()),
                (files == null ? List.<MediaFile>of() : files).stream()
                        .filter(Objects::nonNull)
                        .map(file ->
                                FileUtils.generateAssetUrl(ParentType.REHAB_PLAN, record.getPlanId(), file.getFilename()))
                        .toList());
    }

    /**
     * User: id, username, avatar<br>
     * MediaFile: filename<br>
     * <br>
     * users: RehabRecord.userId<br>
     * files: RehabRecord.id
     */
    public static RehabRecordResponse createBatch(RehabRecord record,
                                                  Map<Long, User> users,
                                                  Map<Long, List<MediaFile>> files) {
        return create(record, users.get(record.getUserId()), files.get(record.getId()));
    }
}
