package com.example.backend.dto;

import com.example.backend.util.StringUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ExaminationFileUploadTable implements ITable {

    private String name;

    @NotNull
    private MultipartFile file;

    public String getName(String defaultName) {
        return StringUtils.hasText(name) ? name : defaultName;
    }
}
