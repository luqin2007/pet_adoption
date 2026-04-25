package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 流浪宠物信息管理模块<br>
 * - 信息录入 ( √ × )<br>
 * ---- 添加流浪宠物信息：addPet ( √ × )<br>
 * ---- 添加宠物位置信息：addLocation ( √ × )<br>
 * - 状态管理 ( √ × )<br>
 * ---- 修改宠物状态: updateStatus ( √ × )<br>
 * - 信息更新 ( √ × )<br>
 * ---- 修改流浪宠物信息：updatePet ( √ × )<br>
 * ---- 删除流浪宠物信息：deletePet ( √ × )<br>
 * - 信息查询 ( √ × )<br>
 * ---- 获取流浪宠物列表：getPets ( √ × )<br>
 * ---- 获取流浪宠物信息：getPet ( √ × )<br>
 * - 多媒体管理 ( √ × )<br>
 * ---- 上传流浪宠物图片/视频：uploadMedia ( √ × )<br>
 * ---- 删除流浪宠物图片/视频：deleteMedia ( √ × )<br>
 * ---- 修改流浪宠物图片信息/视频：updateMedia ( √ × )<br>
 * - 特征信息管理 ( √ × )<br>
 * ---- 添加流浪宠物特征：addTags ( √ × )<br>
 * ---- 删除流浪宠物特征：deleteTags ( √ × )<br>
 * - 状态流转记录 ( √ × )<br>
 * ---- 修改宠物状态: getStatusRecords ( √ × )
 */
@Validated
@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    /**
     * 添加流浪宠物信息
     */
    @PostMapping({"", "/"})
    public Result<PetAddResponse> addPet(@Valid @RequestBody PetInfoAddRequest petInformation) {
        PetAddResponse response = petService.addPet(petInformation);
        return Result.success(response);
    }

    /**
     * 添加宠物位置信息
     */
    @PostMapping("/{id}/location")
    public Result<PetResponse> addLocation(@PathVariable("id") Long petId,
                                           @Valid @RequestBody LocationRequest request) {
        PetResponse response = petService.addLocation(petId, request);
        return Result.success(response);
    }

    /**
     * 获取流浪宠物列表
     */
    @GetMapping({"", "/"})
    public Result<Page<PetResponse>> getPets(@Valid PetQueryParams query, PageParams page) {
        Page<PetResponse> response = petService.getPets(query, page);
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
    @PutMapping("/{id}")
    public Result<PetResponse> updatePet(@PathVariable("id") Long petId,
                                         @Valid @RequestBody PetUpdateRequest request) {
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
    @PostMapping("/{id}/media")
    public Result<PetMediaResponse> uploadMedia(@PathVariable("id") Long petId,
                                                @ModelAttribute PetMediaUploadTable file) {
        PetMediaResponse response = petService.uploadMedia(petId, file);
        return Result.success(response);
    }

    /**
     * 修改流浪宠物图片/视频信息
     */
    @PutMapping("/{id}/media/{mid}")
    public Result<PetMediaResponse> updateMedia(@PathVariable("id") Long petId,
                                                @PathVariable("mid") Long mediaId,
                                                @Valid @RequestBody PetMediaUpdateRequest request) {
        PetMediaResponse response = petService.updateMedia(petId, mediaId, request);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物图片/视频
     */
    @DeleteMapping("/{id}/media/{mid}")
    public Result<Void> deleteMedia(@PathVariable("id") Long petId, @PathVariable("mid") Long mediaId) {
        petService.deleteMedia(petId, mediaId);
        return Result.success();
    }

    /**
     * 添加流浪宠物特征
     */
    @PostMapping("/{id}/tags")
    public Result<List<PetTagResponse>> addTags(@PathVariable("id") Long petId,
                                                @Valid @RequestBody PetTagAddRequest request) {
        List<PetTagResponse> response = petService.addTags(petId, request);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物特征
     */
    @DeleteMapping("/{id}/tags")
    public Result<List<PetTagResponse>> deleteTags(@PathVariable("id") Long petId,
                                                   @Valid @RequestBody IdsRequest request) {
        List<PetTagResponse> response = petService.deleteTags(petId, request);
        return Result.success(response);
    }

    @PutMapping("/{id}/status")
    public Result<PetStatusRecordResponse> updateStatus(@PathVariable("id") Long petId,
                                                        @Valid @RequestBody PetStatusUpdateRequest request) {
        PetStatusRecordResponse response = petService.updateStatus(petId, request);
        return Result.success(response);
    }

    /**
     * 获取宠物状态流转记录
     */
    @GetMapping("/{id}/status")
    public Result<Page<PetStatusRecordResponse>> getStatusRecords(@PathVariable("id") Long petId, PageParams pageParams) {
        Page<PetStatusRecordResponse> response = petService.getStatusRecords(petId, pageParams, Set.of());
        return Result.success(response);
    }
}
