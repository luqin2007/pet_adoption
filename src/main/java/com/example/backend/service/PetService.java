package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.query.PetLocations;
import com.example.backend.event.PetAddEvent;
import com.example.backend.event.PetLocationEvent;
import com.example.backend.event.PetStatusChangeEvent;
import com.example.backend.event.PetUpdateEvent;
import com.example.backend.mapper.PetLocationMapper;
import com.example.backend.mapper.PetMapper;
import com.example.backend.mapper.PetStatusRecordMapper;
import com.example.backend.mapper.PetTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.backend.entity.property.ParentType.PET;

/**
 * 宠物管理
 */
@Service
@RequiredArgsConstructor
public class PetService extends BaseService<PetMapper, Pet> {

    private final PetStatusRecordMapper petStatusRecordMapper;
    private final PetLocationMapper petLocationMapper;
    private final PetTagMapper petTagMapper;

    private FileService fileService;
    private UserService userService;
    private MedicalService medicalService;

    /**
     * 添加宠物基础信息
     */
    @Transactional
    public PetAddResponse addPet(PetInfoAddRequest request) {
        // 权限校验
        User login = requireLoginUser();
        // 宠物信息
        Pet pet = request.createInfo(login.getId());
        save(pet);
        // 位置信息
        Location location = request.createLocation(pet.getId(), login.getId());
        petLocationMapper.insert(location);

        eventPublisher.publishEvent(new PetAddEvent(pet, login, location));
        return PetAddResponse.create(pet);
    }

    /**
     * 添加宠物位置信息
     */
    @Transactional
    public PetResponse addLocation(Long petId, LocationRequest request) {
        User login = requireLoginUser();
        Location location = request.createLocation(petId, login.getId());
        petLocationMapper.insert(location);
        eventPublisher.publishEvent(new PetLocationEvent(location, login));
        return getPet(petId);
    }

    /**
     * 获取流浪宠物列表
     */
    public Page<PetResponse> getPets(PetQueryParams paramRequest, PageParams pageRequest) {
        Page<PetLocations> result = baseMapper.queryByParams(paramRequest).page(pageRequest);
        Set<Long> petIds = result.getRecords().stream().map(Pet::getId).collect(Collectors.toSet());
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().map(Pet::getDiscoverId),
                User::getId, User::getUsername, User::getAvatar);
        Map<Long, List<PetTagResponse>> tags = petTagMapper.queryByPets(petIds).groupList(PetTag::getPetId, PetTagResponse::create);
        Map<Long, String> covers = fileService.getCoverUrls(PET, petIds);
        Map<Long, List<VaccineResponse>> vaccines = medicalService.getVaccinesByPetIds(petIds);
        Map<Long, List<DewormResponse>> deworms = medicalService.getDewormsByPetIds(petIds);
        return convertDto(result, info -> PetResponse.createBatch(info, users, tags, covers, vaccines, deworms));
    }

    /**
     * 获取宠物基础信息
     */
    public PetResponse getPet(Long petId) {
        Pet info = requireById(petId);
        List<PetTagResponse> tags = getTags(petId);
        User discover = userService.selectById(info.getDiscoverId(), User::getId, User::getUsername, User::getAvatar);
        String cover = fileService.getCoverUrl(petId, PET);
        List<VaccineResponse> vaccines = medicalService.getVaccines(petId);
        List<DewormResponse> deworms = medicalService.getDeworms(petId);
        List<Location> locations = petLocationMapper.queryByPet(petId).list();
        return PetResponse.create(info, cover, discover, tags, vaccines, deworms, locations);
    }

    /**
     * 更新流浪宠物信息
     */
    @Transactional
    public PetResponse updatePet(Long petId, PetUpdateRequest request) {
        // 检查宠物是否存在
        Pet info = requireById(petId);
        User login = requireLoginUser();
        checkUserPermission(info, login);

        // 更新宠物信息
        request.applyTo(info);
        updateById(info);
        eventPublisher.publishEvent(new PetUpdateEvent(info, login));

        User discover = userService.selectById(info.getDiscoverId(), User::getId, User::getUsername, User::getAvatar);
        List<PetTagResponse> tags = getTags(petId);
        String cover = fileService.getCoverUrl(petId, PET);
        List<VaccineResponse> vaccines = medicalService.getVaccines(petId);
        List<DewormResponse> deworms = medicalService.getDeworms(petId);
        List<Location> locations = petLocationMapper.queryByPet(petId).list();
        return PetResponse.create(info, cover, discover, tags, vaccines, deworms, locations);
    }

    /**
     * 删除流浪宠物信息
     */
    @Transactional
    public void deletePet(Long petId) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        getBaseMapper().discardPetById(petId).update();
    }

    /**
     * 上传流浪宠物图片/视频
     */
    @Transactional
    public PetMediaResponse uploadMedia(Long petId, PetMediaUploadTable request) {
        // 检查用户
        User user = requireLoginUser();

        // 保存图片/视频
        MultipartFile file = request.getFile();
        MediaFile media = request.createMedia(petId, user.getId(), file);
        fileService.uploadMediaFile(file, media, petId, PET);
        return PetMediaResponse.create(media);
    }

    /**
     * 更新流浪宠物图片/视频信息
     */
    @Transactional
    public PetMediaResponse updateMedia(Long petId, Long mediaId, PetMediaUpdateRequest request) {
        // 检查权限
        User login = requireLoginUser();
        Pet info = requireById(petId);
        checkUserPermission(info, login);

        // 更新图片信息
        MediaFile media = fileService.updateMediaFile(mediaId, petId, PET, request::applyTo);
        return PetMediaResponse.create(media);
    }

    /**
     * 删除流浪宠物图片/视频
     */
    @Transactional
    public void deleteMedia(Long petId, Long mediaId) {
        // 检查权限
        Pet info = requireById(petId);
        User login = requireLoginUser();
        checkUserPermission(info, login);

        // 删除图片
        fileService.deleteMediaFile(mediaId, petId, PET);
    }

    /**
     * 获取流浪宠物标签
     */
    public List<PetTagResponse> getTags(Long petId) {
        return petTagMapper.queryByPet(petId).list().stream()
                .map(PetTagResponse::create)
                .toList();
    }

    /**
     * 获取流浪宠物标签
     */
    public Map<Long, List<PetTagResponse>> getTags(Set<Long> petIds) {
        return petTagMapper.queryByPets(petIds).groupList(PetTag::getPetId, PetTagResponse::create);
    }

    /**
     * 添加流浪宠物标签
     */
    @Transactional
    public List<PetTagResponse> addTags(Long petId, PetTagAddRequest request) {
        // 检查权限
        Pet info = requireById(petId);
        User login = requireLoginUser();
        checkUserPermission(info, login);

        // 筛选标签
        Set<String> currentTags = getTags(petId).stream()
                .map(PetTagResponse::getTag)
                .collect(Collectors.toSet());
        List<PetTag> tags = request.create(petId, login.getId(), currentTags);

        // 添加标签
        if (!tags.isEmpty()) {
            petTagMapper.insert(tags);
            eventPublisher.publishEvent(new PetUpdateEvent(info, login));
        }

        return getTags(petId);
    }

    /**
     * 删除流浪宠物标签
     */
    @Transactional
    public List<PetTagResponse> deleteTags(Long petId, IdsRequest request) {
        // 检查权限
        Pet info = requireById(petId);
        User login = requireLoginUser();
        checkUserPermission(info, login);

        // 删除标签
        if (!request.getIds().isEmpty()) {
            petTagMapper.deleteByPetAndIds(petId, request.getIds()).delete();
            eventPublisher.publishEvent(new PetUpdateEvent(info, login));
        }

        return getTags(petId);
    }

    /**
     * 更新流浪宠物状态
     */
    @Transactional
    public PetStatusRecordResponse updateStatus(Long petId, PetStatusUpdateRequest request) {
        // 校验权限
        Pet pet = requireById(petId, Pet::getId, Pet::getStatus);
        User login = requireLoginUser();
        checkUserPermission(pet, login);

        // 保存记录
        PetStatusRecord record = request.create(pet, login.getId());
        baseMapper.updateStatus(petId, record.getTo()).update();
        petStatusRecordMapper.insert(record);
        eventPublisher.publishEvent(new PetStatusChangeEvent(record, login));
        String cover = fileService.getCoverUrl(petId, PET);
        return PetStatusRecordResponse.create(record, pet, cover, login);
    }

    /**
     * 获取流浪宠物状态流转记录
     */
    public Page<PetStatusRecordResponse> getStatusRecords(Long petId, PageParams pageParams, Set<Long> userFilter) {
        // 权限校验
        User login = requireLoginUser();
        requirePermission(login.isWorker());

        // 数据查询
        Page<PetStatusRecord> result = petStatusRecordMapper.queryByPet(petId, userFilter).page(pageParams);

        // 数据转换
        List<PetStatusRecord> records = result.getRecords();
        Set<Long> petIds = records.stream().map(PetStatusRecord::getPetId).collect(Collectors.toSet());
        Map<Long, Pet> pets = groupById(petIds,
                Pet::getId, Pet::getName);
        Map<Long, String> covers = fileService.getCoverUrls(PET, petIds);
        Map<Long, User> users = userService.groupById(
                records.stream().map(PetStatusRecord::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        return convertDto(result, record -> PetStatusRecordResponse.createBatch(record, pets, covers, users));
    }

    /*
     * 权限校验
     */
    private void checkUserPermission(Pet info, User login) {
        boolean allowed = login.isWorker() || switch (info.getStatus()) { // 工作人员、管理员在任何情况下都可以修改
            case WAITING, AGAINST -> // 待审核、未通过审核的宠物，可由第一次发现的志愿者修改
                    info.getDiscoverId() != null && Objects.equals(login.getId(), info.getDiscoverId());
            case ADOPTED -> false;
            default -> // 已领养之前，可由志愿者、兽医修改
                    login.isVolunteer() || login.isDoctor();
        };
        requirePermission(allowed);
    }

    @Autowired
    public void setServices(FileService fileService,
                            UserService userService,
                            MedicalService medicalService) {
        this.fileService = fileService;
        this.userService = userService;
        this.medicalService = medicalService;
    }
}
