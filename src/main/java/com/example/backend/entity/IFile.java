package com.example.backend.entity;

import com.example.backend.util.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public interface IFile {

    String getFilename();

    default String toAssetUrl(Path root) {
        Path urlPath = Paths.get("/assets")
                .resolve(root)
                .resolve(getFilename());
        String url = urlPath.toString();
        if (!Objects.equals(File.separator, "/")) {
            url = url.replace(File.separator, "/");
        }
        return url;
    }

    default String toPetImageUrl(FileUtils fileUtils) {
        Long petId = ((PetImage) this).getPetId();
        Path path = fileUtils.buildPetImagePath(petId);
        return toAssetUrl(path);
    }

    default String toPetVideoUrl(FileUtils fileUtils) {
        Long petId = ((PetVideo) this).getPetId();
        Path path = fileUtils.buildPetVideoPath(petId);
        return toAssetUrl(path);
    }
}
