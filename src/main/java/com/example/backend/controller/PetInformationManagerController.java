package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.service.PetInformationManagerService;
import com.example.backend.service.UserManagerService;
import com.example.backend.util.FileUtils;
import com.example.backend.util.SQLUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流浪宠物信息管理模块
 * - 信息录入 ( √ × )
 *   - 添加流浪宠物信息：addPet ( √ × )
 *   - 删除流浪宠物信息：deletePet ( √ × )
 * - 状态管理 ( √ × )
 * - 信息更新 ( √ × )
 *   - 修改流浪宠物信息：updatePet ( √ × )
 *   - 删除流浪宠物信息：deletePet ( √ × )
 * - 信息查询 ( √ × )
 *   - 获取流浪宠物列表：getPetList ( √ × )
 *   - 获取流浪宠物信息：getPet ( √ × )
 * - 多媒体管理 ( √ × )
 *   - 上传流浪宠物图片：uploadPetImage ( √ × )
 *   - 删除流浪宠物图片：deletePetImage ( √ × )
 *   - 修改流浪宠物图片信息：updatePetImage ( √ × )
 *   - 上传流浪宠物视频：uploadPetVideo ( √ × )
 *   - 删除流浪宠物视频：deletePetVideo ( √ × )
 *   - 修改流浪宠物视频信息：updatePetVideo ( √ × )
 * - 特征信息管理 ( √ × )
 *   - 添加流浪宠物特征：addPetTags ( √ × )
 *   - 删除流浪宠物特征：deletePetTags ( √ × )
 * - 状态流转记录 ( √ × )
 */
@Validated
@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
public class PetInformationManagerController {

    private final PetInformationManagerService petInformationManagerService;
    private final UserManagerService userManagerService;
    private final FileUtils fileUtils;

    /**
     * 添加流浪宠物信息
     */
    @PutMapping("/pets")
    public Result<PetInfoAddResponse> addPet(@RequestBody PetInfoAddRequest petInformation) {
        PetInformation information = petInformationManagerService.addPetInformation(petInformation);
        PetInfoAddResponse response = PetInfoAddResponse.fromEntity(information);
        return Result.success(response);
    }

    /**
     * 获取流浪宠物信息
     */
    @GetMapping("/pets/{id}")
    public Result<PetInfoResponse> getPet(@PathVariable("id") Long petId) {
        PetInformation information = petInformationManagerService.getById(petId);
        User user = userManagerService.getById(information.getUserId());
        List<PetTag> tags = petInformationManagerService.getPetTags(petId);
        PetImage cover = petInformationManagerService.getPetCoverImage(petId).orElse(null);
        PetInfoResponse response = PetInfoResponse.fromEntity(information, user, tags, cover, fileUtils);
        return Result.success(response);
    }

    /**
     * 修改流浪宠物信息
     */
    @PostMapping("/pets/{id}")
    public Result<PetInfoResponse> updatePet(@PathVariable("id") Long petId, @RequestBody PetInfoUpdateRequest request) {
        PetInformation update = petInformationManagerService.updatePetInformation(petId, request);
        User user = userManagerService.getById(update.getUserId());
        List<PetTag> tags = petInformationManagerService.getPetTags(petId);
        PetImage cover = petInformationManagerService.getPetCoverImage(petId).orElse(null);
        PetInfoResponse response = PetInfoResponse.fromEntity(update, user, tags, cover, fileUtils);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物信息
     */
    @DeleteMapping("/pets/{id}")
    public Result<Void> deletePet(@PathVariable("id") Long petId) {
        petInformationManagerService.deletePetInformation(petId);
        return Result.success();
    }

    /**
     * 获取流浪宠物列表
     */
    @GetMapping("/pets")
    public Result<Page<PetInfoResponse>> getPetList(@RequestParam(name = "page", defaultValue = "1") Integer currentPage,
                                                    @RequestParam(name = "size", defaultValue = "10") Integer size,
                                                    @RequestParam(name = "sort", defaultValue = "id") String sort,
                                                    @RequestParam(name = "order", defaultValue = "ASC") String order) {
        Page<PetInformation> page = SQLUtils.createPage(currentPage, size, sort, order);
        Page<PetInformation> result = petInformationManagerService.page(page);
        // 数据转换
        List<PetInformation> records = result.getRecords();
        Map<Long, User> userMap = userManagerService.getUsersBatchByIds(records.stream()
                .map(PetInformation::getUserId)
                .collect(Collectors.toSet()));
        Map<Long, List<PetTag>> tagMap = petInformationManagerService.getPetTagsBatchByPetIds(records.stream()
                .map(PetInformation::getId)
                .collect(Collectors.toSet()));
        Map<Long, PetImage> coverMap = petInformationManagerService.getPetCoversBatchByPetIds(records.stream()
                .map(PetInformation::getId)
                .collect(Collectors.toSet()));
        Page<PetInfoResponse> response = SQLUtils.convertDto(result, info -> {
            User user = userMap.get(info.getUserId());
            List<PetTag> tags = tagMap.getOrDefault(info.getId(), Collections.emptyList());
            PetImage cover = coverMap.get(info.getId());
            return PetInfoResponse.fromEntity(info, user, tags, cover, fileUtils);
        });
        return Result.success(response);
    }

    /**
     * 上传流浪宠物图片
     */
    @PostMapping("/pets/{id}/images")
    public Result<PetMediaResponse> uploadPetImage(@PathVariable("id") Long petId, @RequestBody MultipartFile file) {
        PetImage image = petInformationManagerService.uploadPetImage(petId, file);
        PetMediaResponse response = PetMediaResponse.fromImage(image);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物图片
     */
    @DeleteMapping("/pets/{id}/images")
    public Result<Void> deletePetImage(@PathVariable("id") Long imageId) {
        petInformationManagerService.deletePetImage(imageId);
        return Result.success();
    }

    /**
     * 修改流浪宠物图片信息
     */
    @PostMapping("/pets/{id}/images")
    public Result<PetMediaResponse> updatePetImage(@PathVariable("id") Long petId, @RequestBody PetMediaRequest image) {
        petInformationManagerService.updatePetImage(petId, image);
        return Result.success();
    }

    /**
     * 上传流浪宠物视频
     */
    @PostMapping("/pets/{id}/videos")
    public Result<PetMediaResponse> uploadPetVideo(@PathVariable("id") Long petId, @RequestBody MultipartFile file) {
        PetVideo video = petInformationManagerService.uploadPetVideo(petId, file);
        PetMediaResponse response = PetMediaResponse.fromVideo(video);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物视频
     */
    @DeleteMapping("/pets/{id}/videos")
    public Result<Void> deletePetVideo(@PathVariable("id") Long videoId) {
        petInformationManagerService.deletePetVideo(videoId);
        return Result.success();
    }

    /**
     * 修改流浪宠物视频描述
     */
    @PostMapping("/pets/{id}/videos")
    public Result<PetMediaResponse> updatePetVideo(@PathVariable("id") Long videoId, @RequestBody PetMediaRequest video) {
        petInformationManagerService.updatePetVideo(videoId, video);
        return Result.success();
    }

    /**
     * 添加流浪宠物特征
     */
    @PutMapping("/pets/{id}/tags")
    public Result<List<PetTagResponse>> addPetTags(@PathVariable("id") Long petId, @RequestBody PetTagNamesRequest request) {
        List<PetTag> tags = petInformationManagerService.addPetTags(petId, request);
        List<PetTagResponse> response = tags.stream()
                .map(PetTagResponse::fromTag)
                .toList();
        return Result.success(response);
    }

    /**
     * 删除流浪宠物特征
     */
    @DeleteMapping("/pets/{id}/tags")
    public Result<List<PetTagResponse>> deletePetTags(@PathVariable("id") Long petId, @RequestBody PetTagIdsRequest request) {
        List<PetTag> tags = petInformationManagerService.deletePetTags(petId, request);
        List<PetTagResponse> response = tags.stream()
                .map(PetTagResponse::fromTag)
                .toList();
        return Result.success(response);
    }

    /**
     * 获取宠物状态流转记录
     */
    @GetMapping("/pets/{id}/status")
    public Result<Page<PetStatusRecordResponse>> getPetStatusRecords(@PathVariable("id") Long petId,
                                                                     @RequestParam(name = "page", defaultValue = "1") Integer currentPage,
                                                                     @RequestParam(name = "size", defaultValue = "10") Integer size,
                                                                     @RequestParam(name = "sort", defaultValue = "id") String sort,
                                                                     @RequestParam(name = "order", defaultValue = "ASC") String order) {
        Page<PetStatusRecord> page = SQLUtils.createPage(currentPage, size, sort, order);
        Page<PetStatusRecord> result = petInformationManagerService.getPetStatusRecords(petId, page);
        // 数据转换
        List<PetStatusRecord> records = result.getRecords();
        Map<Long, PetInformation> petMap = petInformationManagerService.getPetInfoBatchByIds(records.stream()
                .map(PetStatusRecord::getPetId)
                .collect(Collectors.toSet()));
        Map<Long, PetImage> coverMap = petInformationManagerService.getPetCoversBatchByPetIds(records.stream()
                .map(PetStatusRecord::getPetId)
                .collect(Collectors.toSet()));
        Map<Long, User> userMap = userManagerService.getUsersBatchByIds(records.stream()
                .map(PetStatusRecord::getUserId)
                .collect(Collectors.toSet()));
        Page<PetStatusRecordResponse> response = SQLUtils.convertDto(result, record -> PetStatusRecordResponse.fromEntity(record,
                petMap.get(record.getPetId()),
                coverMap.get(record.getPetId()),
                userMap.get(record.getUserId()), fileUtils));
        return Result.success(response);
    }
}
