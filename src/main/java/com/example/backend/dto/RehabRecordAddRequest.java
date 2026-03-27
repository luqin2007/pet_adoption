package com.example.backend.dto;

import com.example.backend.entity.RehabRecord;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
public class RehabRecordAddRequest {

    @NotBlank(message = "请输入康复步骤")
    private String step;

    @NotBlank(message = "请输入宠物反应")
    private String reaction;

    @NotBlank(message = "请输入备注")
    private String note;

    private List<MultipartFile> files;

    public RehabRecord create(Long planId, Long doctorId) {
        return new RehabRecord(null,
                planId,
                doctorId,
                step,
                reaction,
                note,
                new Date());
    }
}
