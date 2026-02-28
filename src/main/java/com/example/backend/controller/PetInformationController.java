package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.PetInformation;
import com.example.backend.entity.PetStatusRecord;
import com.example.backend.service.PetInformationService;
import com.example.backend.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
@RequestMapping("/pet")
@RequiredArgsConstructor
public class PetInformationController {

    private final PetInformationService petInformationService;

    /**
     * 添加流浪宠物信息
     */
    @PutMapping("/pets")
    public Result<PetInfoAddResponse> addPetInformation(@RequestBody PetInfoAddRequest petInformation) {
        PetInfoAddResponse response = petInformationService.addPetInformation(petInformation);
        return Result.success(response);
    }

    /**
     * 获取流浪宠物信息
     */
    @GetMapping("/pets/{id}")
    public Result<PetInfoResponse> getPetInformation(@PathVariable("id") Long petId) {
        PetInfoResponse response = petInformationService.getPetInformation(petId);
        return Result.success(response);
    }

    /**
     * 修改流浪宠物信息
     */
    @PostMapping("/pets/{id}")
    public Result<PetInfoResponse> updatePetInformation(@PathVariable("id") Long petId, @RequestBody PetInfoUpdateRequest request) {
        PetInfoResponse response = petInformationService.updatePetInformation(petId, request);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物信息
     */
    @DeleteMapping("/pets/{id}")
    public Result<Void> deletePetInformation(@PathVariable("id") Long petId) {
        petInformationService.deletePetInformation(petId);
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
        Page<PetInformation> page = PageUtils.createPage(currentPage, size, sort, order);
        Page<PetInfoResponse> response = petInformationService.getPetInformationList(page);
        return Result.success(response);
    }

    /**
     * 上传流浪宠物图片
     */
    @PostMapping("/pets/{id}/images")
    public Result<PetMediaResponse> uploadPetImage(@PathVariable("id") Long petId, @RequestBody MultipartFile file) {
        PetMediaResponse response = petInformationService.uploadPetImage(petId, file);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物图片
     */
    @DeleteMapping("/pets/{id}/images")
    public Result<Void> deletePetImage(@PathVariable("id") Long imageId) {
        petInformationService.deletePetImage(imageId);
        return Result.success();
    }

    /**
     * 修改流浪宠物图片信息
     */
    @PostMapping("/pets/{id}/images")
    public Result<PetMediaResponse> updatePetImage(@PathVariable("id") Long petId, @RequestBody PetMediaRequest image) {
        PetMediaResponse response = petInformationService.updatePetImage(petId, image);
        return Result.success(response);
    }

    /**
     * 上传流浪宠物视频
     */
    @PostMapping("/pets/{id}/videos")
    public Result<PetMediaResponse> uploadPetVideo(@PathVariable("id") Long petId, @RequestBody MultipartFile file) {
        PetMediaResponse response = petInformationService.uploadPetVideo(petId, file);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物视频
     */
    @DeleteMapping("/pets/{id}/videos")
    public Result<Void> deletePetVideo(@PathVariable("id") Long videoId) {
        petInformationService.deletePetVideo(videoId);
        return Result.success();
    }

    /**
     * 修改流浪宠物视频描述
     */
    @PostMapping("/pets/{id}/videos")
    public Result<PetMediaResponse> updatePetVideo(@PathVariable("id") Long videoId, @RequestBody PetMediaRequest video) {
        PetMediaResponse response = petInformationService.updatePetVideo(videoId, video);
        return Result.success(response);
    }

    /**
     * 添加流浪宠物特征
     */
    @PutMapping("/pets/{id}/tags")
    public Result<List<PetTagResponse>> addPetTags(@PathVariable("id") Long petId, @RequestBody PetTagNamesRequest request) {
        List<PetTagResponse> response = petInformationService.addPetTags(petId, request);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物特征
     */
    @DeleteMapping("/pets/{id}/tags")
    public Result<List<PetTagResponse>> deletePetTags(@PathVariable("id") Long petId, @RequestBody PetTagIdsRequest request) {
        List<PetTagResponse> response = petInformationService.deletePetTags(petId, request);
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
        Page<PetStatusRecord> page = PageUtils.createPage(currentPage, size, sort, order);
        Page<PetStatusRecordResponse> response = petInformationService.getPetStatusRecords(petId, page);
        return Result.success(response);
    }
}
