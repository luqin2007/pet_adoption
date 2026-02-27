package com.example.backend.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    @Value("${file.upload.path.pets}")
    @Getter
    private String petsUploadPath;

    public Path buildPetPath(Long petId) {
        return Paths.get(petsUploadPath).resolve(String.valueOf(petId));
    }

    public Path buildPetImagePath(Long petId) {
        return buildPetPath(petId).resolve("images");
    }

    public Path buildPetVideoPath(Long petId) {
        return buildPetPath(petId).resolve("videos");
    }

    public Optional<String> getImageExtension(MultipartFile file) {
        String mime = getUpdateFileMime(file);
        String extension = switch (mime) {
            case MediaType.IMAGE_PNG_VALUE -> "png";
            case MediaType.IMAGE_GIF_VALUE -> "gif";
            case MediaType.IMAGE_JPEG_VALUE -> "jpg";
            case "image/bmp" -> "bmp";
            case "image/webp" -> "webp";
            case "image/tiff" -> "tiff";
            default -> {
                logger.warn("getImageExtension: 不支持的图片 MIME: {} {}", mime, file.getOriginalFilename());
                yield null;
            }
        };
        return Optional.ofNullable(extension);
    }

    public Optional<String> getVideoExtension(MultipartFile file) {
        String mime = getUpdateFileMime(file);
        String extension = switch (mime) {
            case "video/mp4", "video/mpeg4" -> "mp4";
            case "video/x-msvideo" -> "avi";
            case "video/x-ms-wmv" -> "wmv";
            case "video/quicktime" -> "mov";
            case "video/x-flv" -> "flv";
            case "video/x-matroska" -> "mkv";
            case "video/3gpp" -> "3gp";
            default -> {
                logger.warn("getVideoExtension: 不支持的视频 MIME: {} {}", mime, file.getOriginalFilename());
                yield null;
            }
        };
        return Optional.ofNullable(extension);
    }

    private String getUpdateFileMime(MultipartFile file) {
        try {
            return new Tika().detect(file.getInputStream());
        } catch (IOException e) {
            logger.warn("getFileMime: 无效的上传文件流: {}", file.getOriginalFilename());
            return "";
        }
    }

    public Path upload(MultipartFile file, String filename, Path targetPath) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("上传文件不能为空");
        }

        Files.createDirectories(targetPath);
        Path filePath = targetPath.resolve(filename);
        if (Files.exists(filePath)) {
            throw new FileAlreadyExistsException("文件已存在");
        }

        file.transferTo(filePath);
        return filePath;
    }

    public void tryDeleteFile(Path file) {
        if (!Files.exists(file)) {
            logger.warn("deleteFile: 文件不存在: {}", file);
            return;
        }

        if (!Files.isRegularFile(file)) {
            logger.warn("deleteFile: 删除目标非文件: {}", file);
            return;
        }

        try {
            Files.delete(file);
            logger.info("deleteDirectory: 文件已删除: {}", file);
        } catch (IOException e) {
            logger.warn("deleteDirectory: 删除文件失败: {}", file, e);
        }
    }

    public void tryDeleteDirectory(Path path, boolean onlyEmpty) {
        if (!Files.exists(path)) {
            logger.warn("deleteDirectory: 目录不存在: {}", path);
            return;
        }

        try (Stream<Path> files = Files.list(path)) {
            boolean isNotEmpty = files.findFirst().isPresent();
            if (onlyEmpty && isNotEmpty) {
                logger.warn("deleteDirectory: 目录非空: {}", path);
                return;
            }

            if (isNotEmpty) {
                logger.warn("deleteDirectory: 删除非空目录: {}", path);
            }

            FileSystemUtils.deleteRecursively(path);
            logger.info("deleteDirectory: 目录已删除: {}", path);
        } catch (IOException e) {
            logger.warn("deleteDirectory: 删除目录失败: {}", path, e);
        }
    }

    public static Optional<String> getNameWithoutExtension(String filename) {
        Optional<String> name = StringUtils.hasText(filename)
                ? Optional.of(filename.substring(0, filename.lastIndexOf(".")))
                : Optional.empty();
        return name.filter(StringUtils::hasText);
    }
}
