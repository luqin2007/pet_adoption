package com.example.backend.dto;

import com.example.backend.entity.ExaminationFile;
import com.example.backend.entity.IFile;
import com.example.backend.entity.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class TempFileInfo implements IFile, Comparable<TempFileInfo> {

    private String filename;
    private String name;
    private Long userId;
    private Integer type;
    private Date createTime;

    @Override
    public int compareTo(TempFileInfo o) {
        return createTime.compareTo(o.createTime);
    }

    public MediaFile createMediaFile(Long parentId, String parentType) {
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
}
