package com.example.backend.util;

import com.example.backend.entity.property.ParentType;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

import static com.example.backend.entity.property.MediaType.IMAGE;
import static com.example.backend.entity.property.MediaType.VIDEO;

/**
 * 文件相关工具类
 */
public class FileUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.systemDefault());
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
    private static final Tika TIKA = new Tika();

    /**
     * 上传文件
     *
     * @param file      文件
     * @param filename  文件名
     * @param directory 存储路径
     */
    public static void upload(MultipartFile file, String filename, Path directory) {
        try {
            if (file.isEmpty()) {
                throw ServiceException.invalidate("exception.invalidate.file.empty");
            }

            Files.createDirectories(directory);
            Path filePath = directory.resolve(filename);
            if (Files.exists(filePath)) {
                throw ServiceException.conflict("exception.conflict.file.exists");
            }

            file.transferTo(filePath);
        } catch (IOException e) {
            LOGGER.warn("upload: 上传文件失败: {}", filename, e);
            throw ServiceException.system("exception.system.file.upload_failed", e);
        }
    }

    /**
     * 获取文件名，不包含扩展名
     */
    public static String getNameWithoutExtension(String filename) {
        if (!StringUtils.hasText(filename)) return "";
        int index = filename.lastIndexOf(".");
        return index < 0 ? filename : filename.substring(0, index);
    }

    /**
     * 获取文件名和扩展名
     */
    public static Pair<String, String> getNameAndExtension(String filename) {
        if (!StringUtils.hasText(filename)) return Pair.of("", "");
        int index = filename.lastIndexOf(".");
        return index < 0 ? Pair.of(filename, "")
                : Pair.of(filename.substring(0, index), filename.substring(index + 1));
    }

    /**
     * 生成上传文件名，日期(yyyyMMddHHmmss) + 名称 + 扩展名
     *
     * @param name       文件名。可空
     * @param createTime 创建时间
     * @param extension  扩展名
     */
    public static String generateFilename(String name, Date createTime, String extension) {
        String prefix = FORMATTER.format(createTime.toInstant());
        if (StringUtils.hasText(extension)) {
            return StringUtils.hasText(name)
                    ? prefix + "_" + name + "." + extension
                    : prefix + "." + extension;
        } else {
            return StringUtils.hasText(name) ? prefix + "_" + name : prefix;
        }
    }

    /**
     * 获取文件扩展名和类型 (ext, type)
     */
    public static Pair<String, com.example.backend.entity.property.MediaType> getFileExtensionAndType(MultipartFile file) {
        try {
            String mime = TIKA.detect(file.getInputStream());
            return switch (mime) {
                // 图片
                case MediaType.IMAGE_PNG_VALUE -> Pair.of("png", IMAGE);
                case MediaType.IMAGE_GIF_VALUE -> Pair.of("gif", IMAGE);
                case MediaType.IMAGE_JPEG_VALUE -> Pair.of("jpg", IMAGE);
                case "image/bmp" -> Pair.of("bmp", IMAGE);
                case "image/webp" -> Pair.of("webp", IMAGE);
                case "image/tiff" -> Pair.of("tiff", IMAGE);
                // 视频
                case "video/mp4", "video/mpeg4" -> Pair.of("mp4", VIDEO);
                case "video/x-msvideo" -> Pair.of("avi", VIDEO);
                case "video/x-ms-wmv" -> Pair.of("wmv", VIDEO);
                case "video/quicktime" -> Pair.of("mov", VIDEO);
                case "video/x-flv" -> Pair.of("flv", VIDEO);
                case "video/x-matroska" -> Pair.of("mkv", VIDEO);
                case "video/3gpp" -> Pair.of("3gp", VIDEO);
                // 未知类型
                default -> {
                    LOGGER.warn("getFileExtensionAndType: 不支持的媒体格式: {} {}", mime, file.getOriginalFilename());
                    throw ServiceException.invalidate("exception.invalidate.file.unsupported_media");
                }
            };
        } catch (IOException e) {
            LOGGER.warn("getFileExtensionAndType: 无效的上传文件流: {}", file.getOriginalFilename());
            throw ServiceException.request("exception.request.file.invalid_stream", e);
        }
    }

    public static Path generatePath(String base, ParentType parentType, Long parentId, String filename) {
        return generatePath(base, parentType, parentId).resolve(filename);
    }

    public static Path generatePath(String base, ParentType parentType, Long parentId) {
        return Paths.get(base)
                .resolve(parentType.getFolder())
                .resolve(String.valueOf(parentId));
    }

    public static Path generatePath(String base, ParentType parentType, String uuid, String filename) {
        return generatePath(base, parentType, uuid).resolve(filename);
    }

    public static Path generatePath(String base, ParentType parentType, String uuid) {
        return Paths.get(base)
                .resolve(parentType.getFolder())
                .resolve(uuid);
    }

    /**
     * 生成资源访问路径
     *
     * @param parentType 关联实体类型
     * @param parentId   关联实体 id
     * @param filename   文件名
     */
    public static String generateAssetUrl(ParentType parentType, Long parentId, String filename) {
        if (!StringUtils.hasText(filename)) return null;
        Path urlPath = Paths.get("/assets")
                .resolve(parentType.getFolder())
                .resolve(String.valueOf(parentId))
                .resolve(filename);
        String url = urlPath.toString();
        if (!Objects.equals(File.separator, "/")) {
            url = url.replace(File.separator, "/");
        }
        return url;
    }

    /**
     * 复制文件内容
     */
    public static void copyFile(Path source, Path target) {
        if (!Files.isRegularFile(source))
            throw ServiceException.system("exception.system.file.not_regular_file");
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target);
        } catch (IOException e) {
            throw ServiceException.system("exception.system.file.copy_failed", e);
        }
    }
}
