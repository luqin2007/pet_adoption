package com.example.backend.dto;

import com.example.backend.entity.DonationFile;
import com.example.backend.entity.ExaminationFile;
import com.example.backend.entity.IFile;
import com.example.backend.entity.MediaFile;
import com.example.backend.entity.property.MediaType;
import com.example.backend.entity.property.ParentType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
public class TempFileInfo implements IFile, Comparable<TempFileInfo>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String filename;
    private String name;
    private Long userId;
    private MediaType type;
    private Date createTime;

    @Override
    public int compareTo(TempFileInfo o) {
        return createTime.compareTo(o.createTime);
    }

    public MediaFile createMediaFile(Long parentId, ParentType parentType) {
        return new MediaFile(null,
                parentId,
                parentType,
                userId,
                name,
                null,
                false,
                filename,
                type,
                createTime);
    }

    public ExaminationFile createExamFile(Long examId) {
        return new ExaminationFile(null,
                examId,
                name,
                filename,
                createTime);
    }

    public DonationFile createDonationFile(Long donationId) {
        return new DonationFile(null,
                donationId,
                type,
                filename,
                createTime);
    }
}
