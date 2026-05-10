package com.example.backend.dto;

import com.example.backend.entity.DonationFile;
import com.example.backend.entity.property.MediaType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

import static com.example.backend.entity.property.ParentType.DONATION;

@Data
@AllArgsConstructor
public class DonationFileResponse implements IResponse {

    private Long id;
    private Long donationId;
    private MediaType type;
    private String assetUrl;
    private Date createTime;

    public static DonationFileResponse create(DonationFile file) {
        return new DonationFileResponse(
                file.getId(),
                file.getDonationId(),
                file.getType(),
                FileUtils.generateAssetUrl(DONATION, file.getDonationId(), file.getFilename()),
                file.getCreateTime());
    }
}
