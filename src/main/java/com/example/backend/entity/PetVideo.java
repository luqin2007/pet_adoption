package com.example.backend.entity;

import com.example.backend.util.FileUtils;
import lombok.Data;

import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Data
public class PetVideo implements IFile {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private Long id;
    private Long petId;
    private Long userId;
    private String name;
    private String filename;
    private String description;
    private Date createTime;

    public static PetVideo createVideo(Long petId, Long userId, String filename, String extension) {
        Date now = new Date(System.currentTimeMillis());
        Optional<String> name = FileUtils.getNameWithoutExtension(filename);
        String prefix = name.orElse("v");

        PetVideo video = new PetVideo();
        video.setPetId(petId);
        video.setUserId(userId);
        video.setName(name.orElse(""));
        video.setFilename(prefix + "_" + FORMATTER.format(now.toInstant()) + "." + extension);
        video.setDescription("");
        video.setCreateTime(now);
        return video;
    }
}
