package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.example.backend.util.DbUtils;
import com.example.backend.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.backend.util.C.*;

@Service
@RequiredArgsConstructor
public class PetService extends BaseService<PetMapper, Pet> {

    private final PetStatusRecordMapper petStatusRecordMapper;
    private final PetLocationMapper petLocationMapper;
    private final MediaInfoMapper mediaInfoMapper;
    private final PetTagMapper petTagMapper;

    private UserService userService;
    private MedicalService medicalService;

    /**
     * 添加宠物基础信息
     */
    @Transactional
    public PetAddResponse addPet(PetInfoAddRequest request) {
        // 权限校验
        User login = getLoginUser();
        // 宠物信息
        Pet information = request.createInfo(login.getId());
        save(information);
        // 位置信息
        Location location = request.createLocation(information.getId(), login.getId());
        petLocationMapper.insert(location);
        return PetAddResponse.create(information);
    }

    /**
     * 获取流浪宠物列表
     */
    public Page<PetResponse> getPets(PageRequest page) {
        Page<Pet> result = page(page.createPage());
        List<Pet> records = result.getRecords();
        Set<Long> petIds = records.stream().map(Pet::getId).collect(Collectors.toSet());
        Set<Long> userIds = records.stream().map(Pet::getDiscoverId).collect(Collectors.toSet());
        Map<Long, User> users = userService.groupById(userIds,
                User::getId, User::getUsername, User::getAvatar);
        Map<Long, List<PetTagResponse>> tags = petTagMapper.groupList(petTagMapper.queryByPets(petIds), PetTag::getPetId, PetTagResponse::create);
        Map<Long, String> covers = getCoversByPetIds(petIds);
        Map<Long, List<VaccineResponse>> vaccines = medicalService.getVaccinesByPetIds(petIds);
        Map<Long, List<DewormResponse>> deworms = medicalService.getDewormsByPetIds(petIds);
        return DbUtils.convertDto(result, info -> PetResponse.createBatch(info, users, tags, covers, vaccines, deworms));
    }

    /**
     * 获取宠物基础信息
     */
    public PetResponse getPet(Long petId) {
        Pet info = requireById(petId);
        List<PetTagResponse> tags = getTags(petId);
        User discover = userService.selectById(info.getDiscoverId(), User::getId, User::getUsername, User::getAvatar);
        String cover = getCoverById(petId);
        List<VaccineResponse> vaccines = medicalService.getVaccines(petId);
        List<DewormResponse> deworms = medicalService.getDeworms(petId);
        return PetResponse.create(info, cover, discover, tags, vaccines, deworms);
    }

    /**
     * 更新流浪宠物信息
     */
    @Transactional
    public PetResponse updatePet(Long petId, PetUpdateRequest request) {
        // 检查宠物是否存在
        Pet info = requireById(petId);
        User login = getLoginUser();
        checkUserPermission(info, login);

        // 更新宠物信息
        request.applyTo(info);
        updateById(info);

        User discover = userService.selectById(info.getDiscoverId(), User::getId, User::getUsername, User::getAvatar);
        List<PetTagResponse> tags = getTags(petId);
        String cover = getCoverById(petId);
        List<VaccineResponse> vaccines = medicalService.getVaccines(petId);
        List<DewormResponse> deworms = medicalService.getDeworms(petId);
        return PetResponse.create(info, cover, discover, tags, vaccines, deworms);
    }

    /**
     * 删除流浪宠物信息
     */
    @Transactional
    public void deletePet(Long petId) {
        User login = getLoginUser();
        requirePermission(login.isWorker());
        update(getBaseMapper().discardPetById(petId));
    }

    /**
     * 上传流浪宠物图片/视频
     */
    @Transactional
    public PetMediaResponse uploadMedia(Long petId, PetMediaUploadRequest request) {
        // 检查用户
        User user = getLoginUser();

        // 保存图片/视频
        MultipartFile file = request.getFile();
        MediaFile media = request.createMedia(petId, user.getId(), file);
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
        return PetMediaResponse.create(media);
    }

    /**
     * 更新流浪宠物图片/视频信息
     */
    @Transactional
    public PetMediaResponse updateMedia(Long mediaId, PetMediaUpdateRequest request) {
        // 检查图片
        MediaFile media = mediaInfoMapper.requireById(mediaId);
        Long petId = media.getParentId();
        Pet info = requireById(petId);

        // 检查权限
        User login = getLoginUser();
        checkUserPermission(info, login);

        // 更新图片信息
        boolean isCoverChanged = !Objects.equals(media.getIsCover(), request.getIsCover());
        request.applyTo(media);
        if (isCoverChanged) { // 切换封面
            if (media.getIsCover()) { // 非封面 -> 封面 清空已有封面
                mediaInfoMapper.update(mediaInfoMapper.clearCover(PARENT_PET, mediaId));
            } else { // 封面 -> 非封面 设置新封面
                if (!mediaInfoMapper.exists(mediaInfoMapper.queryCover(PARENT_PET, petId, mediaId))) {
                    MediaFile latestImage = mediaInfoMapper.selectOne(mediaInfoMapper.queryLatestImageId(PARENT_PET, petId, mediaId));
                    if (latestImage != null)
                        mediaInfoMapper.updateById(latestImage.getId(), MediaFile::getIsCover, true);
                }
            }
        }

        mediaInfoMapper.updateById(media);
        return PetMediaResponse.create(media);
    }

    /**
     * 删除流浪宠物图片/视频
     */
    @Transactional
    public void deleteMedia(Long mediaId) {
        // 检查图片
        MediaFile media = mediaInfoMapper.requireById(mediaId);
        requireEqual(PARENT_PET, media.getParentType(), "图片或视频无效");

        // 检查权限
        Long petId = media.getParentId();
        Pet info = requireById(petId);
        User login = getLoginUser();
        checkUserPermission(info, login);

        // 删除图片
        mediaInfoMapper.deleteById(mediaId);
        Path imgPath = FileUtils.generateFilePath(PARENT_PET, petId, media.getFilename());
        FileUtils.tryDeleteFile(imgPath);

        // 处理封面
        if (MEDIA_TYPE_IMAGE.equals(media.getType()) && media.getIsCover()
                && !mediaInfoMapper.exists(mediaInfoMapper.queryCover(PARENT_PET, petId))) {
            // 没有封面：取最后一张图片为封面
            MediaFile latestImage = mediaInfoMapper.selectOne(mediaInfoMapper.queryLatestImageId(PARENT_PET, petId));
            if (latestImage != null)
                mediaInfoMapper.updateById(latestImage.getId(), MediaFile::getIsCover, true);
        }
    }

    /**
     * 获取宠物封面图片
     */
    public String getCoverById(Long petId) {
        MediaFile info = mediaInfoMapper.selectOne(mediaInfoMapper.queryCoverFilename(PARENT_PET, petId));
        return info == null ? null : FileUtils.generateAssetUrl(PARENT_PET, petId, info.getFilename());
    }

    /**
     * 获取流浪宠物标签
     */
    public List<PetTagResponse> getTags(Long petId) {
        return petTagMapper.selectList(petTagMapper.queryByPet(petId)).stream()
                .map(PetTagResponse::create)
                .toList();
    }

    /**
     * 添加流浪宠物标签
     */
    @Transactional
    public List<PetTagResponse> addTags(Long petId, PetTagAddRequest request) {
        // 检查权限
        Pet info = requireById(petId);
        User login = getLoginUser();
        checkUserPermission(info, login);

        // 筛选标签
        Set<String> currentTags = getTags(petId).stream()
                .map(PetTagResponse::getTag)
                .collect(Collectors.toSet());
        List<PetTag> tags = request.create(petId, login.getId(), currentTags);

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
        Pet info = requireById(petId);
        User login = getLoginUser();
        checkUserPermission(info, login);

        // 删除标签
        if (!request.getIds().isEmpty())
            petTagMapper.delete(petTagMapper.deleteByPetAndIds(petId, request.getIds()));

        return getTags(petId);
    }

    /**
     * 更新流浪宠物状态
     */
    @Transactional
    public PetStatusRecordResponse updateStatus(Long petId, PetStatusUpdateRequest request) {
        // 校验权限
        Pet pet = requireById(petId);
        User login = getLoginUser();
        checkUserPermission(pet, login);

        // 保存记录
        PetStatusRecord record = request.buildStatusRecord(pet, login.getId());
        petStatusRecordMapper.insert(record);
        pet.setStatus(record.getTo());
        updateById(pet);
        return PetStatusRecordResponse.create(record, pet, getCoverById(petId), login);
    }

    /**
     * 获取流浪宠物状态流转记录
     */
    public Page<PetStatusRecordResponse> getStatusRecords(Long petId, PageRequest pageRequest, Set<Long> userFilter) {
        // 权限校验
        User login = getLoginUser();
        requirePermission(login.isWorker());

        // 数据查询
        LambdaQueryWrapper<PetStatusRecord> wrapper = petStatusRecordMapper.queryByPet(petId, userFilter);
        Page<PetStatusRecord> result = petStatusRecordMapper.selectPage(pageRequest.createPage(), wrapper);

        // 数据转换
        List<PetStatusRecord> records = result.getRecords();
        Set<Long> petIds = records.stream().map(PetStatusRecord::getPetId).collect(Collectors.toSet());
        Set<Long> userIds = records.stream().map(PetStatusRecord::getUserId).collect(Collectors.toSet());
        Map<Long, Pet> pets = groupById(petIds, Pet::getId, Pet::getName);
        Map<Long, String> covers = getCoversByPetIds(petIds);
        Map<Long, User> users = userService.groupById(userIds);
        return DbUtils.convertDto(result, record -> PetStatusRecordResponse.createBatch(record, pets, covers, users));
    }

    /*
     * 权限校验
     */
    private void checkUserPermission(Pet info, User login) {
        boolean allowed = login.isWorker() || switch (info.getStatus()) { // 工作人员、管理员在任何情况下都可以修改
            case PET_STATUS_WAITING, PET_STATUS_AGAINST -> // 待审核、未通过审核的宠物，可由第一次发现的志愿者修改
                    info.getDiscoverId() != null && Objects.equals(login.getId(), info.getDiscoverId());
            case PET_STATUS_ADOPTED -> false;
            default -> // 已领养之前，可由志愿者、兽医修改
                    login.isVolunteer() || login.isDoctor();
        };
        requirePermission(allowed);
    }

    /**
     * 根据 id 批量获取流浪宠物封面地址
     */
    public Map<Long, String> getCoversByPetIds(Set<Long> petIds) {
        return mediaInfoMapper.group(mediaInfoMapper.queryCoverFilenames(PARENT_PET, petIds), MediaFile::getFilename);
    }

    @Autowired
    public void setServices(UserService userService,
                            MedicalService medicalService) {
        this.userService = userService;
        this.medicalService = medicalService;
    }
}
