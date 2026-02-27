package com.example.backend.entity;

import com.example.backend.util.FileUtils;
import lombok.Data;

import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Data
public class PetImage implements IFile {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private Long id;
    private Long petId;
    private Long userId;
    private String name;
    private String filename;
    private String description;
    private Boolean isCover;
    private Date createTime;

    public static PetImage createImage(Long petId, Long userId, String filename, String extension) {
        Date now = new Date(System.currentTimeMillis());
        Optional<String> name = FileUtils.getNameWithoutExtension(filename);
        String prefix = name.orElse("p");

        PetImage image = new PetImage();
        image.setPetId(petId);
        image.setUserId(userId);
        image.setName(name.orElse(""));
        image.setFilename(prefix + "_" + FORMATTER.format(now.toInstant()) + "." + extension);
        image.setDescription("");
        image.setIsCover(false);
        image.setCreateTime(now);
        return image;
    }
}
