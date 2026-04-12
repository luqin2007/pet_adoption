package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.ClaimStatus;
import com.example.backend.entity.property.LostPetStatus;
import com.example.backend.entity.property.PetStatus;
import com.example.backend.event.LostPetClaimAddEvent;
import com.example.backend.event.LostPetClaimApproveEvent;
import com.example.backend.mapper.LostPetClaimMapper;
import com.example.backend.mapper.LostPetLocationMapper;
import com.example.backend.mapper.LostPetMapper;
import com.example.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.backend.entity.property.ParentType.LOST_PET;
import static com.example.backend.entity.property.ParentType.PET;

/**
 * 走失宠物报备与智能匹配服务
 */
@Service
@RequiredArgsConstructor
public class LostPetService extends BaseService<LostPetMapper, LostPet> {

    private final LostPetLocationMapper lostPetLocationMapper;
    private final LostPetClaimMapper lostPetClaimMapper;

    private FileService fileService;
    private UserService userService;
    private PetService petService;

    @Value("${key.lost_pet.uuid}")
    private String lostPetKey;
    @Value("${key.lost_pet.file}")
    private String lostPetFileKey;

    /**
     * 准备报备走失
     */
    public String beginLostPet() {
        requireLoginUser();
        String uuid = StringUtils.randomUUID(lostPetKey, redisHelper, 10);
        String redisKey = String.format(lostPetFileKey, uuid);
        redisHelper.putString(redisKey, "", 30);
        return uuid;
    }

    /**
     * 报备走失宠物
     */
    @Transactional
    public LostPetResponse addLostPet(LostPetAddRequest request) {
        // 校验登录状态
        User login = requireLoginUser();
        String uuid = request.getUuid();
        requireRedisUuid(lostPetKey, uuid);

        // 保存数据
        LostPet lostPet = request.create(login.getId());
        save(lostPet);
        Location location = request.createLocation(lostPet.getId(), login.getId());
        lostPetLocationMapper.insert(location);

        // 转移临时文件
        fileService.saveTempMedias(lostPetFileKey, uuid, lostPet, LOST_PET);

        // 查找可能的宠物
        PetQueryParams params = request.createQuery();
        PageParams page = new PageParams();
        Page<PetResponse> pets = petService.getPets(params, page);
        return buildLostPetResponse(lostPet, login, location, pets.getRecords());
    }

    /**
     * 上传宠物图片
     */
    public String uploadLostPetMedia(String uuid, LostPetMediaUploadTable request) {
        requireLoginUser();
        requireRedisUuid(lostPetKey, uuid);
        return fileService
                .uploadTempMedia(request.getFile(), request.getName(), uuid, lostPetFileKey, LOST_PET)
                .getFilename();
    }

    /**
     * 删除宠物图片
     */
    public void deleteLostPetMedia(String uuid, String filename) {
        requireLoginUser();
        requireRedisUuid(lostPetKey, uuid);
        fileService.deleteTempFile(lostPetFileKey, uuid, filename, LOST_PET);
    }

    /**
     * 修改走失宠物
     */
    @Transactional
    public LostPetResponse updateLostPet(Long lostPetId, LostPetUpdateRequest request) {
        User login = requireLoginUser();
        LostPet lostPet = requireById(lostPetId);
        requirePermission(login.is(lostPet.getOwnerId()) || login.isWorker());

        request.applyTo(lostPet);
        updateById(lostPet);
        Location location = lostPetLocationMapper.selectById(lostPet.getId());
        request.applyTo(location);
        lostPetLocationMapper.updateById(location);

        // 查找可能的宠物
        PetQueryParams params = request.createQuery();
        PageParams page = new PageParams();
        Page<PetResponse> pets = petService.getPets(params, page);
        return buildLostPetResponse(lostPet, login, location, pets.getRecords());
    }

    /**
     * 获取走失宠物
     */
    public LostPetResponse getLostPet(Long lostPetId) {
        LostPet lostPet = requireById(lostPetId);
        return buildLostPetResponse(lostPet, null, null, List.of());
    }

    private LostPetResponse buildLostPetResponse(LostPet lostPet, User owner, Location location, List<PetResponse> pets) {
        if (owner == null || !owner.is(lostPet.getOwnerId()))
            owner = userService.requireById(lostPet.getOwnerId(),
                    User::getId, User::getUsername, User::getAvatar);

        if (location == null)
            location = lostPetLocationMapper.selectById(lostPet.getId());

        Pet pet = lostPet.getPetId() == null ? null : petService.requireById(lostPet.getPetId(),
                Pet::getId, Pet::getName);
        String cover = pet == null ? null : fileService.getCoverUrl(pet.getId(), PET);

        return LostPetResponse.create(lostPet, location, owner, pet, cover, pets);
    }

    /**
     * 查询走失宠物
     */
    public Page<LostPetResponse> getLostPets(LostPetQueryParams params, PageParams pageParams) {
        Page<? extends LostPet> result;
        Map<Long, Location> locations;
        // 根据参数不同，尽量避免联表查询
        if (params.noLocation()) {
            result = baseMapper.queryByRequest(params).page(pageParams);
            Set<Long> petIds = result.getRecords().stream().map(LostPet::getId).collect(Collectors.toSet());
            locations = lostPetLocationMapper
                    .queryByLostPets(petIds)
                    .group(Location::getParentId);
        } else if (params.noPet()) {
            Page<Location> pl = lostPetLocationMapper.queryByRequest(params).page(pageParams);
            result = convertDto(pl, // 1:1
                    location -> requireById(location.getParentId()));
            locations = pl.getRecords().stream()
                    .collect(Collectors.toMap(Location::getParentId, Function.identity()));
        } else {
            result = baseMapper.queryByRequestLocation(params).page(pageParams);
            locations = Map.of();
        }

        // 组装其他成员
        Map<Long, User> owners = userService.groupById(
                result.getRecords().stream().map(LostPet::getOwnerId),
                User::getId, User::getUsername, User::getAvatar);
        Map<Long, Pet> pets = petService.groupById(
                result.getRecords().stream().map(LostPet::getPetId),
                Pet::getId, Pet::getName);
        Map<Long, String> petCovers = fileService.getCoverUrls(PET,
                pets.values().stream().map(Pet::getId).collect(Collectors.toSet()));
        return convertDto(result, lostPet ->
                LostPetResponse.createBatch(lostPet, locations, owners, pets, petCovers));
    }

    /**
     * 发起认领申请
     */
    @Transactional
    public LostPetClaimResponse addClaim(LostPetClaimAddRequest request) {
        User login = requireLoginUser();
        LostPet lostPet = requireById(request.getLostPetId());
        require(lostPet.getStatus() == LostPetStatus.SEARCHING, "exception.invalidate.lost_pet.claim_unavailable");

        // 保存申请
        LostPetClaim claim = request.create(login.getId());
        lostPetClaimMapper.insert(claim);

        // 通知
        eventPublisher.publishEvent(new LostPetClaimAddEvent(claim, login));
        return buildClaimResponse(claim);
    }

    /**
     * 获取认领申请
     */
    public LostPetClaimResponse getClaim(Long claimId) {
        LostPetClaim claim = lostPetClaimMapper.requireById(claimId);
        return buildClaimResponse(claim);
    }

    /**
     * 查询认领申请
     */
    public Page<LostPetClaimResponse> getClaims(ClaimQueryParams paramRequest, PageParams pageRequest) {
        Page<LostPetClaim> result = lostPetClaimMapper
                .queryByRequest(paramRequest)
                .page(pageRequest);
        Map<Long, LostPet> lostPets = groupById(
                result.getRecords().stream().map(LostPetClaim::getLostPetId),
                LostPet::getId, LostPet::getName, LostPet::getType, LostPet::getBreed);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(claim -> Stream.of(claim.getApplicantId(), claim.getReviewerId())),
                User::getId, User::getUsername, User::getAvatar);
        return convertDto(result, claim ->
                LostPetClaimResponse.createBatch(claim, lostPets, users));
    }

    /**
     * 取消认领申请
     */
    public void cancelClaim(Long claimId) {
        User login = requireLoginUser();
        @SuppressWarnings("unchecked")
        LostPetClaim claim = lostPetClaimMapper.requireById(claimId,
                LostPetClaim::getId, LostPetClaim::getStatus, LostPetClaim::getApplicantId);
        requirePermission(login.is(claim.getApplicantId()) || login.isWorker());
        require(claim.getStatus() == ClaimStatus.PENDING, "exception.invalidate.claim.cancel_pending_only");
        lostPetClaimMapper.deleteById(claimId);
    }

    /**
     * 认领申请审核
     */
    @Transactional
    public LostPetClaimResponse approveClaim(Long claimId, ClaimApproveRequest request) {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        LostPetClaim claim = lostPetClaimMapper.requireById(claimId);
        require(claim.getStatus() == ClaimStatus.PENDING, "exception.invalidate.claim.review_pending_only");
        if (request.getPetId() != null)
            petService.requireExist(request.getPetId());

        // 更新申请状态
        ClaimStatus status = ClaimStatus.get(request.getStatus());
        lostPetClaimMapper.approve(claimId, status, request.getReason()).update();
        // 更新走失宠物状态为已认领
        if (status == ClaimStatus.PASS) {
            baseMapper.updateStatus(claim.getLostPetId(), request.getPetId(), LostPetStatus.CLAIMED);
            if (request.getPetId() != null) { // 更新宠物状态
                PetStatusUpdateRequest sr = new PetStatusUpdateRequest();
                sr.setPetId(request.getPetId());
                sr.setStatus(PetStatus.HOME.name());
                sr.setReason(request.getReason());
                petService.updateStatus(request.getPetId(), sr);
            }
        }

        // 通知申请人
        eventPublisher.publishEvent(new LostPetClaimApproveEvent(claim));
        return buildClaimResponse(claim);
    }

    private LostPetClaimResponse buildClaimResponse(LostPetClaim claim) {
        LostPet lostPet = requireById(claim.getLostPetId(),
                LostPet::getId, LostPet::getPetId, LostPet::getName, LostPet::getType, LostPet::getBreed);
        User applicant = userService.requireById(claim.getApplicantId(),
                User::getId, User::getUsername, User::getAvatar);
        User reviewer = claim.getReviewerId() != null
                ? userService.selectById(claim.getReviewerId(),
                User::getId, User::getUsername, User::getAvatar)
                : null;
        return LostPetClaimResponse.create(claim, lostPet, applicant, reviewer);
    }

    @Autowired
    public void setServices(FileService fileService,
                            UserService userService,
                            PetService petService) {
        this.fileService = fileService;
        this.userService = userService;
        this.petService = petService;
    }
}
