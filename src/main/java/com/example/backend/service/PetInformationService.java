package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.util.*;
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

@Service
@RequiredArgsConstructor
public class PetInformationService extends ServiceImpl<PetInformationMapper, PetInformation> {

    private final PetStatusRecordMapper petStatusRecordMapper;
    private final PetLocationMapper petLocationMapper;
    private final PetImageMapper petImageMapper;
    private final PetVideoMapper petVideoMapper;
    private final PetTagMapper petTagMapper;

    private final UserService userService;
    private final AuthUtils authUtils;
    private final FileUtils fileUtils;

    /**
     * 添加流浪宠物基础信息
     */
    @Transactional
    public PetInfoAddResponse addPetInformation(PetInfoAddRequest request) {
        PetInformation information = request.createInfo();
        save(information);
        PetLocation location = request.createLocation(information.getId());
        petLocationMapper.insert(location);
        return PetInfoAddResponse.fromEntity(information);
    }

    public PetInfoResponse getPetInformation(Long petId) {
        PetInformation info = getById(petId);
        UserResponse user = userService.getUser(info.getUserId(), true);
        List<PetTagResponse> tags = getPetTags(petId);
        String cover = getPetCoverImage(petId)
                .map(image -> image.toPetImageUrl(fileUtils))
                .orElse(null);
        return PetInfoResponse.fromEntity(info, user, tags, cover);
    }

    /**
     * 更新流浪宠物信息
     */
    @Transactional
    public PetInfoResponse updatePetInformation(Long petId, PetInfoUpdateRequest request) {
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

        // 更新宠物信息
        request.apply(info);
        info.setUpdateTime(updateTime);
        updateById(info);
        UserResponse discover = userService.getUser(info.getUserId(), true);
        List<PetTagResponse> tags = getPetTags(petId);
        String cover = getPetCoverImage(petId)
                .map(image -> image.toPetImageUrl(fileUtils))
                .orElse(null);
        return PetInfoResponse.fromEntity(info, discover, tags, cover);
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
     * 获取流浪宠物列表
     */
    public Page<PetInfoResponse> getPetInformationList(Page<PetInformation> page) {
        Page<PetInformation> result = page(page);
        // 数据转换
        List<PetInformation> records = result.getRecords();
        Map<Long, UserResponse> userMap = userService.getUsersBatchByIds(records.stream()
                .map(PetInformation::getUserId)
                .collect(Collectors.toSet()));
        Map<Long, List<PetTagResponse>> tagMap = getPetTagsBatchByPetIds(records.stream()
                .map(PetInformation::getId)
                .collect(Collectors.toSet()));
        Map<Long, PetImage> coverMap = getPetCoversBatchByPetIds(records.stream()
                .map(PetInformation::getId)
                .collect(Collectors.toSet()));
        return PageUtils.convertDto(result, info -> {
            UserResponse user = userMap.get(info.getUserId());
            List<PetTagResponse> tags = tagMap.getOrDefault(info.getId(), Collections.emptyList());
            String cover = Optional.ofNullable(coverMap.get(info.getId()))
                    .map(image -> image.toPetImageUrl(fileUtils))
                    .orElse(null);
            return PetInfoResponse.fromEntity(info, user, tags, cover);
        });
    }

    /**
     * 上传流浪宠物图片
     */
    @Transactional
    public PetMediaResponse uploadPetImage(Long petId, MultipartFile file) {
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
        return PetMediaResponse.fromImage(image, fileUtils);
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
    public PetMediaResponse updatePetImage(Long imageId, PetMediaRequest request) {
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

        return PetMediaResponse.fromImage(image, fileUtils);
    }

    /**
     * 上传流浪宠物视频
     */
    @Transactional
    public PetMediaResponse uploadPetVideo(Long petId, MultipartFile file) {
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
        return PetMediaResponse.fromVideo(video, fileUtils);
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
    public PetMediaResponse updatePetVideo(Long videoId, PetMediaRequest request) {
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

        return PetMediaResponse.fromVideo(video, fileUtils);
    }

    /*
     * 权限校验: 工作人员、管理员在任何情况下都可以修改
     * - 待审核、未通过审核的宠物，可由第一次发现的志愿者修改
     * - 已领养之前，可由志愿者、兽医修改
     */
    private boolean checkUserPermission(PetInformation info, User login) {
        return login.isWorker() || switch (info.getStatus()) {
            case 0, 1 -> info.getUserId() != null && Objects.equals(login.getId(), info.getUserId());
            case 7 -> false;
            default -> login.isWorker() || login.isVolunteer();
        };
    }

    /**
     * 获取流浪宠物标签
     */
    public List<PetTagResponse> getPetTags(Long petId) {
        return petTagMapper.selectList(Wrappers.<PetTag>lambdaQuery().eq(PetTag::getPetId, petId))
                .stream()
                .map(PetTagResponse::fromTag)
                .toList();
    }

    /**
     * 添加流浪宠物标签
     */
    @Transactional
    public List<PetTagResponse> addPetTags(Long petId, PetTagNamesRequest request) {
        // 检查权限
        PetInformation info = getOptById(petId)
                .orElseThrow(() -> new ServiceException("流浪宠物不存在"));
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!checkUserPermission(info, login))
            throw new ServiceException("权限不足");

        // 筛选标签
        Set<String> currentTags = getPetTags(petId).stream()
                .map(PetTagResponse::getTag)
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
    public List<PetTagResponse> deletePetTags(Long petId, PetTagIdsRequest request) {
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
    public Page<PetStatusRecordResponse> getPetStatusRecords(Long petId, Page<PetStatusRecord> page) {
        User login = authUtils.getLoginUser(ServiceException::new);
        if (!login.isWorker())
            throw new ServiceException("权限不足");

        Page<PetStatusRecord> result = petStatusRecordMapper.selectPage(page, Wrappers.<PetStatusRecord>lambdaQuery()
                .eq(PetStatusRecord::getPetId, petId));
        // 数据转换
        List<PetStatusRecord> records = result.getRecords();
        Map<Long, PetInformation> petMap = getPetInfoBatchByIds(records.stream()
                .map(PetStatusRecord::getPetId)
                .collect(Collectors.toSet()));
        Map<Long, PetImage> coverMap = getPetCoversBatchByPetIds(records.stream()
                .map(PetStatusRecord::getPetId)
                .collect(Collectors.toSet()));
        Map<Long, UserResponse> userMap = userService.getUsersBatchByIds(records.stream()
                .map(PetStatusRecord::getUserId)
                .collect(Collectors.toSet()));
        return PageUtils.convertDto(result, record -> PetStatusRecordResponse.fromEntity(record,
                petMap.get(record.getPetId()),
                coverMap.get(record.getPetId()).toPetImageUrl(fileUtils),
                userMap.get(record.getUserId())));
    }

    /**
     * 根据 id 批量获取流浪宠物标签
     */
    public Map<Long, List<PetTagResponse>> getPetTagsBatchByPetIds(Set<Long> petIds) {
        Map<Long, List<PetTagResponse>> map = new HashMap<>(petIds.size());
        petIds.forEach(id -> map.put(id, new ArrayList<>()));
        petTagMapper.selectList(Wrappers.<PetTag>lambdaQuery().in(PetTag::getPetId, petIds))
                .forEach(tag -> map.get(tag.getPetId()).add(PetTagResponse.fromTag(tag)));
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
