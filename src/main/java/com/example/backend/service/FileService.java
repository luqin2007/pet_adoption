package com.example.backend.service;

import com.example.backend.dto.TempFileInfo;
import com.example.backend.entity.DeleteJob;
import com.example.backend.entity.IId;
import com.example.backend.entity.MediaFile;
import com.example.backend.entity.User;
import com.example.backend.entity.property.MediaType;
import com.example.backend.entity.property.ParentType;
import com.example.backend.mapper.DeleteJobMapper;
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

    private final DeleteJobMapper deleteJobMapper;

    @Value("${application.key_timeout}")
    private long keyTimeout;
    @Value("${file.upload}")
    private String upload;
    @Value("${file.temp}")
    private String temp;

    /**
     * 向临时目录中上传图片/视频
     */
    public TempFileInfo uploadTempMedia(MultipartFile file, String name, String uuid, String fileTemplate, ParentType parentType) {
        User user = requireLoginUser();
        // 上传文件
        Pair<String, MediaType> extAndType = FileUtils.getFileExtensionAndType(file);
        Date now = new Date();
        String nameWithoutExt = FileUtils.getNameWithoutExtension(file.getOriginalFilename());
        name = StringUtils.hasText(name) ? name : nameWithoutExt;
        String filename = FileUtils.generateFilename(nameWithoutExt, now, extAndType.getFirst());
        Path targetPath = FileUtils.generatePath(temp, parentType, uuid);
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
    public TempFileInfo uploadTempFile(MultipartFile file, String name, String uuid, String fileTemplate, ParentType parentType) {
        User user = requireLoginUser();
        // 上传文件
        Date now = new Date();
        Pair<String, String> nameAndExt = FileUtils.getNameAndExtension(file.getOriginalFilename());
        String filename = FileUtils.generateFilename(nameAndExt.getFirst(), now, nameAndExt.getSecond());
        Path targetPath = FileUtils.generatePath(temp, parentType, uuid);
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
    @Transactional
    public void deleteTempFile(String fileKeyTemplate, String uuid, String filename, ParentType parentType) {
        // 查找文件
        String fileKey = String.format(fileKeyTemplate, uuid);
        List<TempFileInfo> files = redisHelper.getAndDeleteObjectsFromHash(fileKey, filename);

        // 删除文件
        TempFileInfo fileInfo = files.get(0);
        Path path = FileUtils.generatePath(temp, parentType, uuid, fileInfo.getFilename());
        deleteJobMapper.insert(DeleteJob.create(path));

        // 刷新超时
        redisHelper.expireObject(fileKey, keyTimeout);
    }

    @Transactional
    public void deleteFile(String filename, Long parentId, ParentType parentType) {
        if (StringUtils.hasText(filename)) {
            Path path = FileUtils.generatePath(upload, parentType, parentId, filename);
            deleteJobMapper.insert(DeleteJob.create(path));
        }
    }

    /**
     * 将临时目录中的文件转移到目标目录
     */
    @Transactional
    public Stream<TempFileInfo> saveTempFiles(String fileKeyTemplate, String uuid, IId parent, ParentType parentType) {
        // 转移文件
        String fileKey = String.format(fileKeyTemplate, uuid);
        List<TempFileInfo> files = redisHelper.getObjectsFromHash(fileKey, TempFileInfo.class).toList();
        for (TempFileInfo file : files) {
            Path source = FileUtils.generatePath(temp, parentType, uuid, file.getFilename());
            Path target = FileUtils.generatePath(upload, parentType, parent.getId(), file.getFilename());
            FileUtils.copyFile(source, target);
        }

        // 清理文件 / Redis
        Path path = FileUtils.generatePath(temp, parentType, uuid);
        deleteJobMapper.insert(DeleteJob.create(path));
        redisHelper.deleteObject(fileKey);

        return files.stream().sorted();
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
     * 上传图片，不经临时目录，不加入数据库
     *
     * @return 文件名
     */
    public String uploadImage(MultipartFile file, Long parentId, ParentType parentType) {
        requireLoginUser();

        // 保存图片/视频
        Path resources = FileUtils.generatePath(upload, parentType, parentId);
        Date now = new Date();
        Pair<String, MediaType> extAndType = FileUtils.getFileExtensionAndType(file);
        requireEqual(MediaType.IMAGE, extAndType.getSecond(), "不支持的图片格式");
        String name = FileUtils.getNameWithoutExtension(file.getOriginalFilename());
        String filename = FileUtils.generateFilename(name, now, extAndType.getFirst());
        FileUtils.upload(file, filename, resources);
        return filename;
    }

    /**
     * 上传图片，不经临时目录，不加入数据库
     *
     * @return 文件名和上传完成时间
     */
    public List<Pair<String, Date>> uploadImages(List<MultipartFile> files, Long parentId, ParentType parentType) {
        requireLoginUser();

        // 检查文件类型
        List<Pair<String, MediaType>> extAndTypes = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            Pair<String, MediaType> extAndType = FileUtils.getFileExtensionAndType(file);
            requireEqual(MediaType.IMAGE, extAndType.getSecond(), "不支持的图片格式");
            extAndTypes.add(extAndType);
        }

        // 上传文件
        Path resources = FileUtils.generatePath(upload, parentType, parentId);
        List<Pair<String, Date>> nameAndUpdateTimes = new ArrayList<>(files.size());
        for (int i = 0; i < files.size(); i++) {
            Date now = new Date();
            Pair<String, MediaType> extAndType = extAndTypes.get(i);
            String name = FileUtils.getNameWithoutExtension(files.get(i).getOriginalFilename());
            String filename = FileUtils.generateFilename(name, now, extAndType.getFirst());
            FileUtils.upload(files.get(i), filename, resources);
            nameAndUpdateTimes.add(Pair.of(filename, new Date()));
        }
        return nameAndUpdateTimes;
    }

    /**
     * 上传媒体文件（不经临时文件）
     */
    @Transactional
    public void uploadMediaFile(MultipartFile file, MediaFile media, Long parentId, ParentType parentType) {
        requireLoginUser();

        // 保存图片/视频
        Path resources = FileUtils.generatePath(upload, parentType, parentId);
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
        Path resources = FileUtils.generatePath(upload, parentType, parentId);
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
        Path file = FileUtils.generatePath(upload, parentType, media.getParentId(), media.getFilename());
        deleteJobMapper.insert(DeleteJob.create(file));

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
        Path taskPath = FileUtils.generatePath(upload, parentType, parentId);
        deleteJobMapper.insert(DeleteJob.create(taskPath));
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
