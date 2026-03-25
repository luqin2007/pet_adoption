package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 流浪宠物信息管理模块
 * - 信息录入 ( √ × )
 * ---- 添加流浪宠物信息：addPet ( √ × )
 * ---- 删除流浪宠物信息：deletePet ( √ × )
 * - 状态管理 ( √ × )
 * ---- 修改宠物状态: updateStatus ( √ × )
 * - 信息更新 ( √ × )
 * ---- 修改流浪宠物信息：updatePet ( √ × )
 * ---- 删除流浪宠物信息：deletePet ( √ × )
 * - 信息查询 ( √ × )
 * ---- 获取流浪宠物列表：getPets ( √ × )
 * ---- 获取流浪宠物信息：getPet ( √ × )
 * - 多媒体管理 ( √ × )
 * ---- 上传流浪宠物图片/视频：uploadMedia ( √ × )
 * ---- 删除流浪宠物图片/视频：deleteMedia ( √ × )
 * ---- 修改流浪宠物图片信息/视频：updateMedia ( √ × )
 * - 特征信息管理 ( √ × )
 * ---- 添加流浪宠物特征：addTags ( √ × )
 * ---- 删除流浪宠物特征：deleteTags ( √ × )
 * - 状态流转记录 ( √ × )
 * ---- 修改宠物状态: getStatusRecords ( √ × )
 */
@Validated
@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    /**
     * 添加流浪宠物信息
     */
    @PostMapping("/")
    public Result<PetAddResponse> addPet(@RequestBody PetInfoAddRequest petInformation) {
        PetAddResponse response = petService.addPet(petInformation);
        return Result.success(response);
    }

    /**
     * 获取流浪宠物列表
     */
    @GetMapping("/")
    public Result<Page<PetResponse>> getPets(PageRequest pageRequest) {
        Page<PetResponse> response = petService.getPets(pageRequest);
        return Result.success(response);
    }

    /**
     * 获取流浪宠物信息
     */
    @GetMapping("/{id}")
    public Result<PetResponse> getPet(@PathVariable("id") Long petId) {
        PetResponse response = petService.getPet(petId);
        return Result.success(response);
    }

    /**
     * 修改流浪宠物信息
     */
    @PostMapping("/{id}")
    public Result<PetResponse> updatePet(@PathVariable("id") Long petId, @RequestBody PetUpdateRequest request) {
        PetResponse response = petService.updatePet(petId, request);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物信息
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePet(@PathVariable("id") Long petId) {
        petService.deletePet(petId);
        return Result.success();
    }

    /**
     * 上传流浪宠物图片/视频，使用 multipart/form-data
     */
    @PutMapping("/{id}/media")
    public Result<PetMediaResponse> uploadMedia(@PathVariable("id") Long petId, PetMediaUploadRequest file) {
        PetMediaResponse response = petService.uploadMedia(petId, file);
        return Result.success(response);
    }

    /**
     * 修改流浪宠物图片/视频信息
     */
    @PostMapping("/{id}/media")
    public Result<PetMediaResponse> updateMedia(@PathVariable("id") Long petId, @RequestBody PetMediaUpdateRequest image) {
        PetMediaResponse response = petService.updateMedia(petId, image);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物图片/视频
     */
    @DeleteMapping("/{id}/media")
    public Result<Void> deleteMedia(@PathVariable("id") Long imageId) {
        petService.deleteMedia(imageId);
        return Result.success();
    }

    /**
     * 添加流浪宠物特征
     */
    @PutMapping("/pets/{id}/tags")
    public Result<List<PetTagResponse>> addTags(@PathVariable("id") Long petId, @RequestBody PetTagAddRequest request) {
        List<PetTagResponse> response = petService.addTags(petId, request);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物特征
     */
    @DeleteMapping("/pets/{id}/tags")
    public Result<List<PetTagResponse>> deleteTags(@PathVariable("id") Long petId, @RequestBody IdsRequest request) {
        List<PetTagResponse> response = petService.deleteTags(petId, request);
        return Result.success(response);
    }

    @PostMapping("/pets/{id}/status")
    public Result<PetStatusRecordResponse> updateStatus(@PathVariable("id") Long petId,
                                                        @RequestBody PetStatusUpdateRequest request) {
        PetStatusRecordResponse response = petService.updateStatus(petId, request);
        return Result.success(response);
    }

    /**
     * 获取宠物状态流转记录
     */
    @GetMapping("/pets/{id}/status")
    public Result<Page<PetStatusRecordResponse>> getStatusRecords(@PathVariable("id") Long petId, PageRequest pageRequest) {
        Page<PetStatusRecordResponse> response = petService.getStatusRecords(petId, pageRequest, Set.of());
        return Result.success(response);
    }
}
