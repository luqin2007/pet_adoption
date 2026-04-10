package com.example.backend.service;

import com.example.backend.dto.TempFileInfo;
import com.example.backend.entity.ExaminationFile;
import com.example.backend.entity.IId;
import com.example.backend.entity.MediaFile;
import com.example.backend.entity.User;
import com.example.backend.entity.property.MediaType;
import com.example.backend.entity.property.ParentType;
import com.example.backend.mapper.ExaminationFileMapper;
import com.example.backend.mapper.MediaFileMapper;
import com.example.backend.util.FileUtils;
import com.example.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.example.backend.entity.property.MediaType.IMAGE;

@Service
@RequiredArgsConstructor
public class FileService extends BaseService<MediaFileMapper, MediaFile> {

    private final ExaminationFileMapper examinationFileMapper;

    @Value("${application.key_timeout}")
    private long keyTimeout;

    /**
     * 向临时目录中上传图片/视频
     */
    public TempFileInfo uploadMediaToTemp(MultipartFile file, String name, String uuid, String fileTemplate, ParentType parentType) {
        User user = requireLoginUser();
        // 上传文件
        Pair<String, MediaType> extAndType = FileUtils.getFileExtensionAndType(file);
        Date now = new Date();
        String nameWithoutExt = FileUtils.getNameWithoutExtension(file.getOriginalFilename());
        name = StringUtils.hasText(name) ? name : nameWithoutExt;
        String filename = FileUtils.generateFilename(nameWithoutExt, now, extAndType.getFirst());
        Path targetPath = FileUtils.generateTempPath(parentType, uuid);
        FileUtils.upload(file, filename, targetPath);

        // 存储媒体数据
        TempFileInfo fileInfo = new TempFileInfo(
                filename,
                name,
                user.getId(),
                extAndType.getSecond(),
                now);
        String hashKey = String.format(fileTemplate, uuid);
        redisHelper.putObjectToHash(hashKey, filename, fileInfo);
        redisHelper.expireObject(hashKey, keyTimeout);
        return fileInfo;
    }

    /**
     * 向临时目录中上传任意文件
     */
    public TempFileInfo uploadFileToTemp(MultipartFile file, String name, String uuid, String fileTemplate, ParentType parentType) {
        User user = requireLoginUser();
        // 上传文件
        Date now = new Date();
        Pair<String, String> nameAndExt = FileUtils.getNameAndExtension(file.getOriginalFilename());
        String filename = FileUtils.generateFilename(nameAndExt.getFirst(), now, nameAndExt.getSecond());
        Path targetPath = FileUtils.generateTempPath(parentType, uuid);
        FileUtils.upload(file, filename, targetPath);

        // 存储媒体数据
        TempFileInfo fileInfo = new TempFileInfo(
                filename,
                name == null ? filename : name,
                user.getId(),
                IMAGE,
                now);
        String hashKey = String.format(fileTemplate, uuid);
        redisHelper.putObjectToHash(hashKey, filename, fileInfo);
        redisHelper.expireObject(hashKey, keyTimeout);
        return fileInfo;
    }

    /**
     * 从临时目录中删除文件
     */
    public void deleteTempFile(String fileKeyTemplate, String uuid, String filename, ParentType parentType) {
        // 查找文件
        String fileKey = String.format(fileKeyTemplate, uuid);
        List<TempFileInfo> files = redisHelper.getAndDeleteObjectsFromHash(fileKey, filename);
        if (files.isEmpty()) return;

        // 删除文件
        TempFileInfo fileInfo = files.get(0);
        Path file = FileUtils.generateTempPath(parentType, uuid, fileInfo.getFilename());
        FileUtils.tryDeleteFile(file);
        FileUtils.tryDeleteDirectory(file.getParent(), true);

        // 刷新超时
        redisHelper.expireObject(fileKey, keyTimeout);
    }

    /**
     * 将临时目录中的文件转移到目标目录
     */
    public Stream<TempFileInfo> saveTempFiles(String fileKeyTemplate, String uuid, IId parent, ParentType parentType) {
        // 转移文件
        String fileKey = String.format(fileKeyTemplate, uuid);
        Stream<TempFileInfo> files = redisHelper.getObjectsFromHash(fileKey, TempFileInfo.class)
                // 转移临时文件
                .filter(file -> FileUtils.transferTempFile(file, uuid, parent.getId(), parentType))
                // 更新媒体信息
                .sorted();

        // 清理文件 / Redis
        Path path = FileUtils.generateTempPath(parentType, uuid);
        FileUtils.tryDeleteDirectory(path, false);
        redisHelper.deleteObject(fileKey);

        return files;
    }

    /**
     * 将临时目录中的文件图片/视频到目标目录
     */
    @Transactional
    public void saveTempMedias(String fileKeyTemplate, String uuid, IId parent, ParentType parentType) {
        // 转移文件
        List<MediaFile> files = saveTempFiles(fileKeyTemplate, uuid, parent, parentType)
                .map(file -> file.createMediaFile(parent.getId(), parentType))
                .toList();
        // 检查封面
        if (!files.isEmpty()) {
            long coverCount = files.stream().filter(MediaFile::getIsCover).count();
            if (coverCount > 1) {
                files.stream()
                        .filter(MediaFile::getIsCover)
                        .skip(1)
                        .forEach(file -> file.setIsCover(false));
            } else if (coverCount == 0) {
                files.stream()
                        .filter(file -> file.getType() == IMAGE)
                        .findFirst()
                        .ifPresent(file -> file.setIsCover(Boolean.TRUE));
            }
        }
        saveBatch(files);
    }

    /**
     * 将临时目录中的文件图片/视频到目标目录
     */
    @Transactional
    public List<ExaminationFile> saveTempExaminations(String fileKeyTemplate, String uuid, IId parent) {
        // 转移文件
        List<ExaminationFile> files = saveTempFiles(fileKeyTemplate, uuid, parent, ParentType.EXAMINATION)
                .map(info -> info.createExamFile(parent.getId()))
                .toList();
        examinationFileMapper.insert(files);
        return files;
    }

    /**
     * 上传媒体文件（不经临时文件）
     */
    @Transactional
    public void uploadMediaFile(MultipartFile file, MediaFile media, Long parentId, ParentType parentType) {
        requireLoginUser();

        // 保存图片/视频
        Path resources = FileUtils.generateFilePath(parentType, parentId);
        FileUtils.upload(file, media.getFilename(), resources);

        // 检查封面
        if (IMAGE == media.getType()) {
            if (Boolean.TRUE.equals(media.getIsCover())) { // 封面：清理旧封面
                baseMapper.clearCover(parentType, parentId).update();
            } else { // 非封面：若原本没有封面则设置为封面
                boolean hasCover = baseMapper.queryCover(parentType, parentId).exists();
                media.setIsCover(!hasCover);
            }
        }
        save(media);
    }

    /**
     * 上传媒体文件（不经临时文件，不检查封面）
     */
    @Transactional
    public List<MediaFile> uploadMediaFiles(List<MultipartFile> files, Long parentId, ParentType parentType) {
        User user = requireLoginUser();

        // 保存图片/视频
        Path resources = FileUtils.generateFilePath(parentType, parentId);
        List<MediaFile> mediaFiles = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            Date now = new Date();
            Pair<String, MediaType> extAndType = FileUtils.getFileExtensionAndType(file);
            String name = FileUtils.getNameWithoutExtension(file.getOriginalFilename());
            String filename = FileUtils.generateFilename(name, now, extAndType.getFirst());
            FileUtils.upload(file, filename, resources);

            MediaFile mediaFile = new MediaFile(null,
                    parentId,
                    parentType,
                    user.getId(),
                    filename,
                    "",
                    false,
                    filename,
                    extAndType.getSecond(),
                    now);
            mediaFiles.add(mediaFile);
        }
        saveBatch(mediaFiles);
        return mediaFiles;
    }

    /**
     * 修改媒体文件信息
     */
    @Transactional
    public MediaFile updateMediaFile(Long mediaId, Long parentId, ParentType parentType, Consumer<MediaFile> updater) {
        // 检查图片
        requireLoginUser();
        MediaFile media = requireById(mediaId);
        requireEqual(parentType, media.getParentType(), "图片/视频不匹配");
        requireEqual(parentId, media.getParentId(), "图片/视频不匹配");

        // 更新图片信息
        Boolean oldIsCover = media.getIsCover();
        updater.accept(media);
        boolean isCoverChanged = !Objects.equals(oldIsCover, media.getIsCover());
        if (isCoverChanged) { // 切换封面
            if (media.getIsCover()) { // 非封面 -> 封面 清空已有封面
                baseMapper.clearCover(parentType, parentId).update();
            } else { // 封面 -> 非封面 设置新封面
                if (!baseMapper.queryCover(parentType, parentId, mediaId).exists()) {
                    MediaFile latestImage = baseMapper.queryLatestImageId(parentType, parentId, mediaId).one();
                    if (latestImage != null)
                        baseMapper.updateCover(latestImage.getId(), true).update();
                }
            }
        }

        updateById(media);
        return media;
    }

    /**
     * 删除媒体文件
     */
    @Transactional
    public void deleteMediaFile(Long mediaId, Long parentId, ParentType parentType) {
        // 检查文件存在
        requireLoginUser();
        MediaFile media = requireById(mediaId);
        requireEqual(parentType, media.getParentType(), "图片/视频不匹配");
        requireEqual(parentId, media.getParentId(), "图片/视频不匹配");

        // 删除媒体文件
        removeById(mediaId);
        Path file = FileUtils.generateFilePath(parentType, media.getParentId(), media.getFilename());
        FileUtils.tryDeleteFile(file);
        FileUtils.tryDeleteDirectory(file.getParent(), true);

        // 重置封面
        if (IMAGE == media.getType() && Boolean.TRUE.equals(media.getIsCover())) {
            MediaFile latestImage = baseMapper.queryLatestImageId(parentType, parentId).one();
            if (latestImage != null)
                baseMapper.updateCover(latestImage.getId(), true).update();
        }
    }

    /**
     * 删除所有媒体文件
     */
    @Transactional
    public void deleteAllMediaFiles(Long parentId, ParentType parentType) {
        // 删除数据库
        List<MediaFile> mediaFiles = baseMapper.queryFile(parentType, parentId).list();
        removeByIds(mediaFiles);

        // 删除文件
        mediaFiles.stream()
                .map(info -> FileUtils.generateFilePath(parentType, parentId, info.getFilename()))
                .forEach(FileUtils::tryDeleteFile);
        Path taskPath = FileUtils.generateFilePath(parentType, parentId);
        FileUtils.tryDeleteDirectory(taskPath, false);
    }

    /**
     * 获取封面资源 URL
     */
    public String getCoverUrl(Long parentId, ParentType parentType) {
        MediaFile file = baseMapper.queryCover(parentType, parentId).one();
        if (file == null) return null;
        return FileUtils.generateAssetUrl(parentType, parentId, file.getFilename());
    }

    /**
     * 获取封面资源 URL
     */
    public Map<Long, String> getCoverUrls(ParentType parentType, Set<Long> parentIds) {
        List<MediaFile> list = baseMapper.queryCovers(parentType, parentIds).list();
        Map<Long, String> covers = new HashMap<>(list.size());
        for (MediaFile file : list) {
            covers.put(file.getParentId(), FileUtils.generateAssetUrl(parentType, file.getParentId(), file.getFilename()));
        }
        return covers;
    }
}
