package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.util.AuthUtils;
import com.example.backend.util.FileUtils;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.file.PathUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PetInformationManagerService extends ServiceImpl<PetInformationMapper, PetInformation> {

    private final PetStatusRecordMapper petStatusRecordMapper;
    private final PetLocationMapper petLocationMapper;
    private final PetImageMapper petImageMapper;
    private final PetVideoMapper petVideoMapper;
    private final PetTagMapper petTagMapper;
    private final AuthUtils authUtils;
    private final FileUtils fileUtils;

    /**
     * 添加流浪宠物基础信息
     */
    @Transactional
    public PetInformation addPetInformation(PetInfoAddRequest petInformation) {
        PetInformation info = PetInfoAddRequest.createInformation(petInformation);
        save(info);
        PetLocation location = PetInfoAddRequest.createLocation(info.getId(), petInformation);
        petLocationMapper.insert(location);
        return info;
    }

    /**
     * 更新流浪宠物信息
     */
    @Transactional
    public PetInformation updatePetInformation(Long petId, PetInfoUpdateRequest request) {
        // 检查宠物是否存在
        PetInformation info = getOptById(petId)
                .orElseThrow(() -> new ServiceException("宠物信息不存在"));
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!checkUserPermission(info, login)) {
            throw new ServiceException("权限不足");
        }

        // 记录状态变更
        Date updateTime = new Date(System.currentTimeMillis());
        Integer oldStatus = info.getStatus();
        Integer newStatus = request.getStatus();
        if (!Objects.equals(oldStatus, newStatus)) {
            PetStatusRecord record = new PetStatusRecord();
            record.setUserId(login.getId());
            record.setPetId(petId);
            record.setFrom(oldStatus);
            record.setTo(newStatus);
            record.setTime(updateTime);
            record.setDescription(request.getStatusDesc());
            petStatusRecordMapper.insert(record);
        }

        // 更新
        info.setName(request.getName());
        info.setMinAge(request.getMinAge());
        info.setMaxAge(request.getMaxAge());
        info.setSex(request.getSex());
        info.setType(request.getType());
        info.setBreed(request.getBreed());
        info.setHealth(request.getHealth());
        info.setVaccine(request.getVaccine());
        info.setDescription(request.getDescription());
        info.setStatus(request.getStatus());
        info.setUpdateTime(updateTime);
        updateById(info);
        return info;
    }

    /**
     * 删除流浪宠物信息
     */
    @Transactional
    public void deletePetInformation(Long petId) {
        // 检查宠物是否存在
        PetInformation info = getOptById(petId)
                .orElseThrow(() -> new ServiceException("宠物信息不存在"));
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!checkUserPermission(info, login)) {
            throw new ServiceException("权限不足");
        }

        // 删除图片/视频信息
        Path petImagePath = fileUtils.buildPetImagePath(info.getId());
        Path petVideoPath = fileUtils.buildPetVideoPath(info.getId());
        for (PetImage petImage : petImageMapper.selectList(Wrappers.<PetImage>lambdaQuery().eq(PetImage::getPetId, petId))) {
            Path filePath = petImagePath.resolve(petImage.getFilename());
            fileUtils.tryDeleteFile(filePath);
            petImageMapper.deleteById(petImage.getId());
        }
        for (PetVideo petVideo : petVideoMapper.selectList(Wrappers.<PetVideo>lambdaQuery().eq(PetVideo::getPetId, petId))) {
            Path filePath = petVideoPath.resolve(petVideo.getFilename());
            fileUtils.tryDeleteFile(filePath);
            petVideoMapper.deleteById(petVideo.getId());
        }

        // 删除宠物标签信息
        petTagMapper.delete(Wrappers.<PetTag>lambdaQuery().eq(PetTag::getPetId, petId));

        // 删除宠物发现位置信息
        petLocationMapper.delete(Wrappers.<PetLocation>lambdaQuery().eq(PetLocation::getPetId, petId));

        // 删除宠物信息
        removeById(petId);

        // 删除宠物媒体文件夹
        Path petMediaPath = fileUtils.buildPetPath(info.getId());
        fileUtils.tryDeleteDirectory(petMediaPath, false);
    }

    /**
     * 上传流浪宠物图片
     */
    @Transactional
    public PetImage uploadPetImage(Long petId, MultipartFile file) {
        // 检查用户
        User user = authUtils.getLoginUser(ServiceException::new);

        // 检查图片类型
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        String extension = fileUtils.getImageExtension(file)
                .orElseThrow(() -> new ServiceException("不支持的图片类型"));

        // 保存图片
        PetImage image = PetImage.createImage(petId, user.getId(), file.getOriginalFilename(), extension);
        try {
            Path imagePath = fileUtils.buildPetImagePath(petId);
            Files.createDirectories(imagePath);
            image.setIsCover(PathUtils.isEmptyDirectory(imagePath));
            fileUtils.upload(file, image.getFilename(), imagePath);
        } catch (IOException e) {
            throw new ServiceException("文件上传失败", e);
        }
        petImageMapper.insert(image);
        return image;
    }

    /**
     * 获取宠物封面图片
     */
    public Optional<PetImage> getPetCoverImage(Long petId) {
        LambdaQueryWrapper<PetImage> query = Wrappers.<PetImage>lambdaQuery()
                .eq(PetImage::getPetId, petId)
                .eq(PetImage::getIsCover, true);
        PetImage cover = petImageMapper.selectOne(query, false);
        return Optional.ofNullable(cover);
    }

    /**
     * 删除流浪宠物图片
     */
    @Transactional
    public void deletePetImage(Long imageId) {
        // 检查图片
        PetImage image = petImageMapper.selectById(imageId);
        if (image == null) {
            throw new ServiceException("图片不存在");
        }

        // 检查权限
        Long petId = image.getPetId();
        PetInformation info = getOptById(petId)
                .orElseThrow(() -> new ServiceException("宠物信息不存在"));
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!checkUserPermission(info, login)) {
            throw new ServiceException("权限不足");
        }

        // 删除图片
        petImageMapper.deleteById(imageId);
        Path imgPath = fileUtils.buildPetImagePath(petId).resolve(image.getFilename());
        fileUtils.tryDeleteFile(imgPath);

        // 处理封面
        if (image.getIsCover()) {
            boolean hasCover = petImageMapper.exists(Wrappers.<PetImage>lambdaQuery()
                    .eq(PetImage::getPetId, petId)
                    .eq(PetImage::getIsCover, true));
            if (!hasCover) {
                // 没有封面：取最后一张图片为封面
                PetImage lastImage = petImageMapper.selectOne(Wrappers.<PetImage>lambdaQuery()
                        .eq(PetImage::getPetId, petId)
                        .orderByDesc(PetImage::getCreateTime));
                if (lastImage != null) {
                    lastImage.setIsCover(true);
                    petImageMapper.updateById(lastImage);
                }
            }
        }
    }

    /**
     * 更新流浪宠物图片信息
     */
    @Transactional
    public void updatePetImage(Long imageId, PetMediaRequest request) {
        // 检查图片
        PetImage image = petImageMapper.selectById(imageId);
        if (image == null)
            throw new ServiceException("图片不存在");

        // 检查权限
        Long petId = image.getPetId();
        PetInformation info = getOptById(petId)
                .orElseThrow(() -> new ServiceException("宠物信息不存在"));
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!checkUserPermission(info, login))
            throw new ServiceException("权限不足");

        // 更新图片信息
        boolean isCoverChanged = !image.getIsCover().equals(request.getIsCover());
        image.setName(request.getName());
        image.setDescription(request.getDescription());
        image.setIsCover(request.getIsCover());
        petImageMapper.updateById(image);

        // 切换封面
        if (isCoverChanged) {
            if (image.getIsCover()) {
                // 非封面 -> 封面
                petImageMapper.update(Wrappers.<PetImage>lambdaUpdate()
                        .eq(PetImage::getPetId, petId)
                        .ne(PetImage::getId, imageId)
                        .eq(PetImage::getIsCover, true)
                        .set(PetImage::getIsCover, false));
            } else {
                // 封面 -> 非封面
                PetImage lastImage = petImageMapper.selectOne(Wrappers.<PetImage>lambdaQuery()
                        .eq(PetImage::getPetId, petId)
                        .ne(PetImage::getId, imageId)
                        .orderByDesc(PetImage::getCreateTime));
                if (lastImage != null) {
                    lastImage.setIsCover(true);
                    petImageMapper.updateById(lastImage);
                }
            }
        }
    }

    /**
     * 上传流浪宠物视频
     */
    @Transactional
    public PetVideo uploadPetVideo(Long petId, MultipartFile file) {
        // 检查用户
        User user = authUtils.getLoginUser(ServiceException::new);

        // 检查视频类型
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        String extension = fileUtils.getVideoExtension(file)
                .orElseThrow(() -> new ServiceException("不支持的视频类型"));

        // 保存图片
        PetVideo video = PetVideo.createVideo(petId, user.getId(), file.getOriginalFilename(), extension);
        try {
            Path videoPath = fileUtils.buildPetVideoPath(petId);
            Files.createDirectories(videoPath);
            fileUtils.upload(file, video.getFilename(), videoPath);
        } catch (IOException e) {
            throw new ServiceException("文件上传失败", e);
        }
        petVideoMapper.insert(video);
        return video;
    }

    /**
     * 删除流浪宠物视频
     */
    @Transactional
    public void deletePetVideo(Long videoId) {
        // 检查视频
        PetVideo video = petVideoMapper.selectById(videoId);
        if (video == null) {
            throw new ServiceException("视频不存在");
        }

        // 检查权限
        Long petId = video.getPetId();
        PetInformation info = getOptById(videoId)
                .orElseThrow(() -> new ServiceException("宠物信息不存在"));
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!checkUserPermission(info, login)) {
            throw new ServiceException("权限不足");
        }

        // 删除图片
        petVideoMapper.deleteById(videoId);
        Path videoPath = fileUtils.buildPetVideoPath(petId).resolve(video.getFilename());
        fileUtils.tryDeleteFile(videoPath);
    }

    /**
     * 更新流浪宠物视频信息
     */
    @Transactional
    public void updatePetVideo(Long videoId, PetMediaRequest request) {
        // 检查视频
        PetVideo video = petVideoMapper.selectById(videoId);
        if (video == null)
            throw new ServiceException("视频不存在");

        // 检查权限
        Long petId = video.getPetId();
        PetInformation info = getOptById(petId)
                .orElseThrow(() -> new ServiceException("宠物信息不存在"));
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!checkUserPermission(info, login))
            throw new ServiceException("权限不足");

        // 更新视频信息
        video.setName(request.getName());
        video.setDescription(request.getDescription());
        petVideoMapper.updateById(video);
    }

    /*
     * 权限校验: 工作人员、管理员在任何情况下都可以修改
     * - 待审核、未通过审核的宠物，可由第一次发现的志愿者修改
     * - 已领养之前，可由志愿者、兽医修改
     */
    private boolean checkUserPermission(PetInformation info, User login) {
        return authUtils.isWorker(login) || switch (info.getStatus()) {
            case 0, 1 -> info.getUserId() != null && Objects.equals(login.getId(), info.getUserId());
            case 7 -> false;
            default -> authUtils.isWorker(login) || authUtils.isVolunteer(login);
        };
    }

    /**
     * 获取流浪宠物标签
     */
    public List<PetTag> getPetTags(Long petId) {
        return petTagMapper.selectList(Wrappers.<PetTag>lambdaQuery().eq(PetTag::getPetId, petId));
    }

    /**
     * 添加流浪宠物标签
     */
    @Transactional
    public List<PetTag> addPetTags(Long petId, PetTagNamesRequest request) {
        // 检查权限
        PetInformation info = getOptById(petId)
                .orElseThrow(() -> new ServiceException("流浪宠物不存在"));
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!checkUserPermission(info, login))
            throw new ServiceException("权限不足");

        // 筛选标签
        Set<String> currentTags = getPetTags(petId).stream()
                .map(PetTag::getTag)
                .collect(Collectors.toSet());
        Date createTime = new Date(System.currentTimeMillis());
        List<PetTag> tags = request.getTags().stream()
                // 非空
                .filter(StringUtils::hasText)
                // 去重
                .distinct()
                .filter(tag -> !currentTags.contains(tag))
                .map(tag -> {
                    PetTag petTag = new PetTag();
                    petTag.setPetId(petId);
                    petTag.setUserId(login.getId());
                    petTag.setTag(tag);
                    petTag.setCreateTime(createTime);
                    return petTag;
                })
                .toList();

        // 添加标签
        if (!tags.isEmpty()) {
            petTagMapper.insert(tags);
        }
        return getPetTags(petId);
    }

    /**
     * 删除流浪宠物标签
     */
    @Transactional
    public List<PetTag> deletePetTags(Long petId, PetTagIdsRequest request) {
        // 检查权限
        PetInformation info = getOptById(petId)
                .orElseThrow(() -> new ServiceException("流浪宠物不存在"));
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!checkUserPermission(info, login))
            throw new ServiceException("权限不足");

        // 删除标签
        if (!request.getTags().isEmpty()) {
            petTagMapper.delete(Wrappers.<PetTag>lambdaQuery()
                    .in(PetTag::getId, request.getTags())
                    .eq(PetTag::getPetId, petId));
        }
        return getPetTags(petId);
    }

    /**
     * 获取流浪宠物状态流转记录
     */
    public Page<PetStatusRecord> getPetStatusRecords(Long petId, Page<PetStatusRecord> page) {
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!authUtils.isWorker(login))
            throw new ServiceException("权限不足");

        return petStatusRecordMapper.selectPage(page, Wrappers.<PetStatusRecord>lambdaQuery()
                .eq(PetStatusRecord::getPetId, petId));
    }

    /**
     * 根据 id 批量获取流浪宠物标签
     */
    public Map<Long, List<PetTag>> getPetTagsBatchByPetIds(Set<Long> petIds) {
        Map<Long, List<PetTag>> map = new HashMap<>(petIds.size());
        petIds.forEach(id -> map.put(id, new ArrayList<>()));
        petTagMapper.selectList(Wrappers.<PetTag>lambdaQuery().in(PetTag::getPetId, petIds))
                .forEach(tag -> map.get(tag.getPetId()).add(tag));
        return map;
    }

    /**
     * 根据 id 批量获取流浪宠物标签
     */
    public Map<Long, PetImage> getPetCoversBatchByPetIds(Set<Long> petIds) {
        return petImageMapper.selectList(Wrappers.<PetImage>lambdaQuery()
                .eq(PetImage::getIsCover, true)
                .in(PetImage::getPetId, petIds))
                .stream()
                .collect(Collectors.toMap(PetImage::getPetId, Function.identity()));
    }

    public Map<Long, PetInformation> getPetInfoBatchByIds(Set<Long> petIds) {
        return listByIds(petIds).stream()
                .collect(Collectors.toMap(PetInformation::getId, Function.identity()));
    }
}
