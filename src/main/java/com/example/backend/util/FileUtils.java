package com.example.backend.util;

import com.example.backend.entity.IFile;
import com.example.backend.entity.property.ParentType;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;

import static com.example.backend.entity.property.MediaType.IMAGE;
import static com.example.backend.entity.property.MediaType.VIDEO;

/**
 * 文件相关工具类
 */
public class FileUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    public static String UPLOAD_PATH = "./uploads";
    public static String TEMP_UPLOAD_PATH = "./uploads/__temp";

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
                throw ServiceException.request("上传文件不能为空");
            }

            Files.createDirectories(directory);
            Path filePath = directory.resolve(filename);
            if (Files.exists(filePath)) {
                throw ServiceException.invalidate("文件已存在");
            }

            file.transferTo(filePath);
        } catch (IOException e) {
            LOGGER.warn("upload: 上传文件失败: {}", filename, e);
            throw ServiceException.request("文件上传失败", e);
        }
    }

    /**
     * 将临时文件转移到目标位置
     *
     * @param fileInfo   临时文件信息
     * @param uuid       临时实体 id
     * @param parentId   关联实体 id
     * @param parentType 关联实体类型
     */
    public static boolean transferTempFile(IFile fileInfo, String uuid, Long parentId, ParentType parentType) {
        Path filePath = generateFilePath(parentType, parentId, fileInfo.getFilename());
        Path tempPath = generateTempPath(parentType, uuid, fileInfo.getFilename());
        if (Files.isRegularFile(tempPath) && !Files.exists(filePath)) {
            try {
                // 复制文件
                Files.copy(tempPath, filePath);
            } catch (IOException e) {
                LOGGER.warn("saveTempFile: 复制文件失败: {}", filePath, e);
                return false;
            }
            try {
                // 删除临时文件
                Files.deleteIfExists(tempPath);
            } catch (IOException e) {
                LOGGER.warn("saveTempFile: 删除文件失败: {}", filePath, e);
                return true;
            }
        }
        return true;
    }

    /**
     * 删除文件
     */
    public static void tryDeleteFile(Path file) {
        if (!Files.exists(file)) {
            LOGGER.warn("deleteFile: 文件不存在: {}", file);
            return;
        }

        if (!Files.isRegularFile(file)) {
            LOGGER.warn("deleteFile: 删除目标非文件: {}", file);
            return;
        }

        try {
            Files.delete(file);
            LOGGER.info("deleteDirectory: 文件已删除: {}", file);
        } catch (IOException e) {
            LOGGER.warn("deleteDirectory: 删除文件失败: {}", file, e);
        }
    }

    /**
     * 删除目录
     */
    public static void tryDeleteDirectory(Path path, boolean onlyEmpty) {
        if (!Files.exists(path)) {
            LOGGER.warn("deleteDirectory: 目录不存在: {}", path);
            return;
        }

        try (Stream<Path> files = Files.list(path)) {
            boolean isNotEmpty = files.findFirst().isPresent();
            if (onlyEmpty && isNotEmpty) {
                LOGGER.warn("deleteDirectory: 目录非空: {}", path);
                return;
            }

            if (isNotEmpty) {
                LOGGER.warn("deleteDirectory: 删除非空目录: {}", path);
            }

            FileSystemUtils.deleteRecursively(path);
            LOGGER.info("deleteDirectory: 目录已删除: {}", path);
        } catch (IOException e) {
            LOGGER.warn("deleteDirectory: 删除目录失败: {}", path, e);
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
            String mime = new Tika().detect(file.getInputStream());
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
                    throw ServiceException.invalidate("不支持的媒体格式: " + file.getOriginalFilename() + "[" + mime + "]");
                }
            };
        } catch (IOException e) {
            LOGGER.warn("getFileExtensionAndType: 无效的上传文件流: {}", file.getOriginalFilename());
            throw ServiceException.request("无效的上传文件流: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * 生成文件路径
     *
     * @param parentType 关联实体类型
     * @param parentId   关联实体 id
     * @param filename   文件名
     */
    public static Path generateFilePath(ParentType parentType, Long parentId, String filename) {
        return generateFilePath(parentType, parentId).resolve(filename);
    }

    /**
     * 生成目录路径
     *
     * @param parentType 关联实体类型
     * @param parentId   关联实体 id
     */
    public static Path generateFilePath(ParentType parentType, Long parentId) {
        return Paths.get(UPLOAD_PATH)
                .resolve(parentType.getFolder())
                .resolve(String.valueOf(parentId));
    }

    /**
     * 生成临时文件目录路径
     *
     * @param parentType 关联实体类型
     * @param uuid       关联实体临时 id
     */
    public static Path generateTempPath(ParentType parentType, String uuid) {
        return Paths.get(TEMP_UPLOAD_PATH)
                .resolve(parentType.getFolder())
                .resolve(uuid);
    }

    /**
     * 生成临时文件保存路径
     *
     * @param parentType 关联实体类型
     * @param uuid       关联实体临时 id
     * @param filename   文件名
     */
    public static Path generateTempPath(ParentType parentType, String uuid, String filename) {
        return generateTempPath(parentType, uuid).resolve(filename);
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
}
