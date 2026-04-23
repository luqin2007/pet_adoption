package com.example.backend.controller;

import com.example.backend.entity.property.ParentType;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/assets")
public class AssetController {

    private static final Set<String> ASSET_FOLDERS = Arrays.stream(ParentType.values())
            .map(ParentType::getFolder)
            .collect(Collectors.toUnmodifiableSet());

    @Value("${file.upload}")
    private String upload;

    /**
     * 获取上传后的资源文件
     */
    @GetMapping("/{folder}/{parentId}/{filename}")
    public ResponseEntity<Resource> getAsset(@PathVariable String folder,
                                             @PathVariable Long parentId,
                                             @PathVariable String filename) {
        if (!ASSET_FOLDERS.contains(folder)) {
            throw ServiceException.notFound("exception.not_found.asset");
        }
        if (!StringUtils.hasText(filename) || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            throw ServiceException.invalidate("exception.invalidate.asset.filename");
        }

        Path root = Paths.get(upload).toAbsolutePath().normalize();
        Path file = root.resolve(folder)
                .resolve(String.valueOf(parentId))
                .resolve(filename)
                .normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file) || !Files.isReadable(file)) {
            throw ServiceException.notFound("exception.not_found.asset");
        }

        try {
            Resource resource = new UrlResource(file.toUri());
            String contentType = Files.probeContentType(file);
            MediaType mediaType = StringUtils.hasText(contentType)
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM;
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw ServiceException.system("exception.system.asset.invalid_url", e);
        } catch (IOException e) {
            throw ServiceException.system("exception.system.asset.read_failed", e);
        }
    }
}
