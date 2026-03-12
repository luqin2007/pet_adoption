package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.backend.util.C.*;

@Service
@RequiredArgsConstructor
public class PetService extends BaseService<PetMapper, Pet> {

    private final PetStatusRecordMapper petStatusRecordMapper;
    private final PetLocationMapper petLocationMapper;
    private final MediaInfoMapper mediaInfoMapper;
    private final PetTagMapper petTagMapper;

    private final UserService userService;

    /**
     * 添加宠物基础信息
     */
    @Transactional
    public PetAddResponse addPet(PetInfoAddRequest request) {
        // 宠物信息
        Pet information = request.createInfo();
        save(information);
        // 位置信息
        Location location = request.createLocation(information.getId());
        petLocationMapper.insert(location);
        return PetAddResponse.fromEntity(information);
    }

    /**
     * 获取流浪宠物列表
     */
    public Page<PetResponse> getPets(Page<Pet> page) {
        Page<Pet> result = page(page);
        // 数据转换
        List<Pet> records = result.getRecords();
        Set<Long> petIds = records.stream().map(Pet::getId).collect(Collectors.toSet());
        Set<Long> userIds = records.stream().map(Pet::getUserId).collect(Collectors.toSet());
        Map<Long, UsernameAndAvatarResponse> userMap = userService.getUsernameAndAvatarBatchByIds(userIds);
        Map<Long, List<PetTagResponse>> tagMap = getTagsBatchByPetIds(petIds);
        Map<Long, String> coverMap = getCoverUrlsBatchByPetIds(petIds);
        return DbUtils.convertDto(result, info -> {
            UsernameAndAvatarResponse user = userMap.get(info.getUserId());
            List<PetTagResponse> tags = tagMap.getOrDefault(info.getId(), List.of());
            return PetResponse.fromEntity(info, user, tags, coverMap.get(info.getId()));
        });
    }

    /**
     * 获取宠物基础信息
     */
    public PetResponse getPet(Long petId) {
        Pet info = requireById(petId, "宠物信息不存在");
        UsernameAndAvatarResponse user = userService.getUsernameAndAvatar(info.getUserId());
        List<PetTagResponse> tags = getTags(petId);
        String cover = getCoverUrl(petId);
        return PetResponse.fromEntity(info, user, tags, cover);
    }

    /**
     * 更新流浪宠物信息
     */
    @Transactional
    public PetResponse updatePet(Long petId, PetUpdateRequest request) {
        // 检查宠物是否存在
        Pet info = requireById(petId, "宠物信息不存在");
        User login = AuthUtils.getLoginUser();
        checkUserPermission(info, login);

        // 更新宠物信息
        request.apply(info);
        updateById(info);
        UsernameAndAvatarResponse discover = userService.getUsernameAndAvatar(info.getUserId());
        List<PetTagResponse> tags = getTags(petId);
        String cover = getCoverUrl(petId);
        return PetResponse.fromEntity(info, discover, tags, cover);
    }

    /**
     * 删除流浪宠物信息
     * * 是否需要删除？or 替换成 discard *
     */
    @Transactional
    public void deletePet(Long petId) {
        // 检查宠物是否存在
        Pet pet = requireById(petId, "宠物信息不存在");
        User login = AuthUtils.getLoginUser();
        checkUserPermission(pet, login);

        // 删除图片/视频信息
        List<MediaInfo> mediaInfos = mediaInfoMapper.selectList(mediaInfoMapper.queryIdAndFilename(PARENT_PET, pet.getId()));
        mediaInfoMapper.deleteByIds(mediaInfos);

        // 删除宠物标签信息
        petTagMapper.deleteBy(PetTag::getPetId, petId);

        // 删除宠物发现位置信息
        petLocationMapper.deleteBy(Location::getParentId, pet.getId());

        // 删除宠物状态记录
        petStatusRecordMapper.deleteBy(PetStatusRecord::getPetId, petId);

        // TODO 删除宠物医疗记录

        // 删除宠物信息
        removeById(petId);

        // 删除宠物媒体文件夹
        Path petMediaPath = FileUtils.generateFilePath(PARENT_PET, pet.getId());
        mediaInfos.stream()
                .map(info -> FileUtils.generateFilePath(PARENT_PET, pet.getId(), info.getFilename()))
                .forEach(FileUtils::tryDeleteFile);
        FileUtils.tryDeleteDirectory(petMediaPath, false);
    }

    /**
     * 上传流浪宠物图片/视频
     */
    @Transactional
    public PetMediaResponse uploadMedia(Long petId, PetMediaUploadRequest request) {
        // 检查用户
        User user = AuthUtils.getLoginUser();

        // 保存图片/视频
        MultipartFile file = request.getFile();
        MediaInfo media = request.createMedia(petId, user.getId(), file);
        Path resources = FileUtils.generateFilePath(PARENT_PET, petId);
        FileUtils.upload(file, media.getFilename(), resources);

        // 检查封面
        if (MEDIA_TYPE_IMAGE.equals(media.getType())) {
            if (Boolean.TRUE.equals(request.getIsCover())) { // 封面：清理旧封面
                mediaInfoMapper.update(mediaInfoMapper.clearCover(PARENT_PET, petId));
            } else { // 非封面：若原本没有封面则设置为封面
                boolean hasCover = mediaInfoMapper.exists(mediaInfoMapper.queryCover(PARENT_PET, petId));
                media.setIsCover(!hasCover);
            }
        }
        mediaInfoMapper.insert(media);
        return PetMediaResponse.fromEntity(media);
    }

    /**
     * 更新流浪宠物图片/视频信息
     */
    @Transactional
    public PetMediaResponse updateMedia(Long mediaId, PetMediaUpdateRequest request) {
        // 检查图片
        MediaInfo media = mediaInfoMapper.requireById(mediaId, "图片不存在");
        Long petId = media.getParentId();
        Pet info = requireById(petId, "宠物信息不存在");

        // 检查权限
        User login = AuthUtils.getLoginUser();
        checkUserPermission(info, login);

        // 更新图片信息
        boolean isCoverChanged = !Objects.equals(media.getIsCover(), request.getIsCover());
        request.apply(media);
        if (isCoverChanged) { // 切换封面
            if (media.getIsCover()) { // 非封面 -> 封面 清空已有封面
                mediaInfoMapper.update(mediaInfoMapper.clearCover(PARENT_PET, mediaId));
            } else { // 封面 -> 非封面 设置新封面
                if (!mediaInfoMapper.exists(mediaInfoMapper.queryCover(PARENT_PET, petId, mediaId))) {
                    MediaInfo latestImage = mediaInfoMapper.selectOne(mediaInfoMapper.queryLatestImageId(PARENT_PET, petId, mediaId));
                    if (latestImage != null)
                        mediaInfoMapper.updateById(latestImage.getId(), MediaInfo::getIsCover, true);
                }
            }
        }

        mediaInfoMapper.updateById(media);
        return PetMediaResponse.fromEntity(media);
    }

    /**
     * 删除流浪宠物图片/视频
     */
    @Transactional
    public void deleteMedia(Long mediaId) {
        // 检查图片
        MediaInfo media = mediaInfoMapper.requireById(mediaId, "图片或视频不存在");
        requireEqual(PARENT_PET, media.getParentType(), "图片或视频无效");

        // 检查权限
        Long petId = media.getParentId();
        Pet info = requireById(petId, "宠物信息不存在");
        User login = AuthUtils.getLoginUser();
        checkUserPermission(info, login);

        // 删除图片
        mediaInfoMapper.deleteById(mediaId);
        Path imgPath = FileUtils.generateFilePath(PARENT_PET, petId, media.getFilename());
        FileUtils.tryDeleteFile(imgPath);

        // 处理封面
        if (MEDIA_TYPE_IMAGE.equals(media.getType()) && media.getIsCover()
                && !mediaInfoMapper.exists(mediaInfoMapper.queryCover(PARENT_PET, petId))) {
            // 没有封面：取最后一张图片为封面
            MediaInfo latestImage = mediaInfoMapper.selectOne(mediaInfoMapper.queryLatestImageId(PARENT_PET, petId));
            if (latestImage != null)
                mediaInfoMapper.updateById(latestImage.getId(), MediaInfo::getIsCover, true);
        }
    }

    /**
     * 获取宠物封面图片
     */
    public String getCoverUrl(Long petId) {
        MediaInfo info = mediaInfoMapper.selectOne(mediaInfoMapper.queryCoverFilename(PARENT_PET, petId));
        return info == null ? null : FileUtils.generateAssetUrl(PARENT_PET, petId, info.getFilename());
    }

    /**
     * 获取流浪宠物标签
     */
    public List<PetTagResponse> getTags(Long petId) {
        return petTagMapper.selectList(petTagMapper.queryByPet(petId))
                .stream()
                .map(PetTagResponse::fromTag)
                .toList();
    }

    /**
     * 添加流浪宠物标签
     */
    @Transactional
    public List<PetTagResponse> addTags(Long petId, PetTagNamesRequest request) {
        // 检查权限
        Pet info = requireById(petId, "流浪宠物不存在");
        User login = AuthUtils.getLoginUser();
        checkUserPermission(info, login);

        // 筛选标签
        Set<String> currentTags = getTags(petId).stream()
                .map(PetTagResponse::getTag)
                .collect(Collectors.toSet());
        List<PetTag> tags = request.createTags(petId, login.getId(), currentTags);

        // 添加标签
        if (!tags.isEmpty())
            petTagMapper.insert(tags);

        return getTags(petId);
    }

    /**
     * 删除流浪宠物标签
     */
    @Transactional
    public List<PetTagResponse> deleteTags(Long petId, IdsRequest request) {
        // 检查权限
        Pet info = requireById(petId, "流浪宠物不存在");
        User login = AuthUtils.getLoginUser();
        checkUserPermission(info, login);

        // 删除标签
        if (!request.getIds().isEmpty())
            petTagMapper.delete(petTagMapper.deleteByIdsFilterByPet(petId, request.getIds()));

        return getTags(petId);
    }

    /**
     * 更新流浪宠物状态
     */
    @Transactional
    public PetStatusRecordResponse updateStatus(Long petId, PetStatusUpdateRequest request) {
        // 校验权限
        Pet pet = requireById(petId, "流浪宠物不存在");
        User login = AuthUtils.getLoginUser();
        checkUserPermission(pet, login);

        // 保存记录
        PetStatusRecord record = request.buildStatusRecord(pet, login.getId());
        petStatusRecordMapper.insert(record);
        pet.setStatus(record.getStatus());
        updateById(pet);
        UsernameAndAvatarResponse user = UsernameAndAvatarResponse.fromEntity(login);
        return PetStatusRecordResponse.fromEntity(record, pet, getCoverUrl(petId), user);
    }

    /**
     * 获取流浪宠物状态流转记录
     */
    public Page<PetStatusRecordResponse> getStatusRecords(Long petId, Page<PetStatusRecord> page, Set<Long> userFilter) {
        // 权限校验
        User login = AuthUtils.getLoginUser();
        requirePermission(login.isWorker());

        // 数据查询
        LambdaQueryWrapper<PetStatusRecord> wrapper = petStatusRecordMapper.queryByPet(petId, userFilter);
        Page<PetStatusRecord> result = petStatusRecordMapper.selectPage(page, wrapper);

        // 数据转换
        List<PetStatusRecord> records = result.getRecords();
        Set<Long> petIds = records.stream().map(PetStatusRecord::getPetId).collect(Collectors.toSet());
        Set<Long> userIds = records.stream().map(PetStatusRecord::getUserId).collect(Collectors.toSet());
        Map<Long, Pet> petMap = getInfoBatchByIds(petIds);
        Map<Long, String> coverMap = getCoverUrlsBatchByPetIds(petIds);
        Map<Long, UsernameAndAvatarResponse> userMap = userService.getUsernameAndAvatarBatchByIds(userIds);
        return DbUtils.convertDto(result, record -> PetStatusRecordResponse.fromEntity(record,
                petMap.get(record.getPetId()), coverMap.get(record.getPetId()), userMap.get(record.getUserId())));
    }

    /*
     * 权限校验
     */
    private void checkUserPermission(Pet info, User login) {
        boolean allowed = login.isWorker() || switch (info.getStatus()) { // 工作人员、管理员在任何情况下都可以修改
            case PET_STATUS_WAITING, PET_STATUS_NOT_APPROVED -> // 待审核、未通过审核的宠物，可由第一次发现的志愿者修改
                    info.getUserId() != null && Objects.equals(login.getId(), info.getUserId());
            case PET_STATUS_ADOPTED -> false;
            default -> // 已领养之前，可由志愿者、兽医修改
                    login.isVolunteer() || login.isVeterinarian();
        };
        requirePermission(allowed);
    }

    /**
     * 根据 id 批量获取流浪宠物标签
     */
    public Map<Long, List<PetTagResponse>> getTagsBatchByPetIds(Set<Long> petIds) {
        Map<Long, List<PetTagResponse>> map = new HashMap<>(petIds.size());
        petIds.forEach(id -> map.put(id, new ArrayList<>()));
        petTagMapper.selectList(petTagMapper.queryByPets(petIds))
                .forEach(tag -> map.get(tag.getPetId()).add(PetTagResponse.fromTag(tag)));
        return map;
    }

    /**
     * 根据 id 批量获取流浪宠物封面地址
     */
    public Map<Long, String> getCoverUrlsBatchByPetIds(Set<Long> petIds) {
        return mediaInfoMapper.selectList(mediaInfoMapper.queryCoverFilenames(PARENT_PET, petIds)).stream()
                .collect(Func.toIdMap(info ->
                        FileUtils.generateAssetUrl(PARENT_PET, info.getParentId(), info.getFilename())));
    }

    /**
     * 根据流浪宠物 id 批量获取流浪宠物信息
     */
    public Map<Long, Pet> getInfoBatchByIds(Set<Long> petIds) {
        return listByIds(petIds).stream().collect(Func.toIdMap());
    }
}
