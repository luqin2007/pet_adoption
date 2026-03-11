package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.Pet;
import com.example.backend.entity.PetStatusRecord;
import com.example.backend.service.PetService;
import com.example.backend.util.DbUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

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
 *   - 上传流浪宠物图片/视频：uploadPetMedia ( √ × )
 *   - 删除流浪宠物图片/视频：deletePetMedia ( √ × )
 *   - 修改流浪宠物图片信息/视频：updatePetMedia ( √ × )
 * - 特征信息管理 ( √ × )
 *   - 添加流浪宠物特征：addPetTags ( √ × )
 *   - 删除流浪宠物特征：deletePetTags ( √ × )
 * - 状态流转记录 ( √ × )
 */
@Validated
@RestController
@RequestMapping("/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    /**
     * 添加流浪宠物信息
     */
    @PutMapping("/pets")
    public Result<PetInfoAddResponse> addPetInformation(@RequestBody PetInfoAddRequest petInformation) {
        PetInfoAddResponse response = petService.addInformation(petInformation);
        return Result.success(response);
    }

    /**
     * 获取流浪宠物信息
     */
    @GetMapping("/pets/{id}")
    public Result<PetInfoResponse> getPetInformation(@PathVariable("id") Long petId) {
        PetInfoResponse response = petService.getInformation(petId);
        return Result.success(response);
    }

    /**
     * 修改流浪宠物信息
     */
    @PostMapping("/pets/{id}")
    public Result<PetInfoResponse> updatePetInformation(@PathVariable("id") Long petId, @RequestBody PetInfoUpdateRequest request) {
        PetInfoResponse response = petService.updateInformation(petId, request);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物信息
     */
    @DeleteMapping("/pets/{id}")
    public Result<Void> deletePetInformation(@PathVariable("id") Long petId) {
        petService.deleteInformation(petId);
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
        Page<Pet> page = DbUtils.createPage(currentPage, size, sort, order);
        Page<PetInfoResponse> response = petService.getInformations(page);
        return Result.success(response);
    }

    /**
     * 上传流浪宠物图片/视频
     */
    @PostMapping("/pets/{id}/media")
    public Result<PetMediaResponse> uploadPetMedia(@PathVariable("id") Long petId, @RequestBody MultipartFile file) {
        PetMediaResponse response = petService.uploadMedia(petId, file);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物图片/视频
     */
    @DeleteMapping("/pets/{id}/media")
    public Result<Void> deletePetMedia(@PathVariable("id") Long imageId) {
        petService.deleteMedia(imageId);
        return Result.success();
    }

    /**
     * 修改流浪宠物图片/视频信息
     */
    @PostMapping("/pets/{id}/media")
    public Result<PetMediaResponse> updatePetMedia(@PathVariable("id") Long petId, @RequestBody PetMediaRequest image) {
        PetMediaResponse response = petService.updateMedia(petId, image);
        return Result.success(response);
    }

    /**
     * 添加流浪宠物特征
     */
    @PutMapping("/pets/{id}/tags")
    public Result<List<PetTagResponse>> addPetTags(@PathVariable("id") Long petId, @RequestBody PetTagNamesRequest request) {
        List<PetTagResponse> response = petService.addTags(petId, request);
        return Result.success(response);
    }

    /**
     * 删除流浪宠物特征
     */
    @DeleteMapping("/pets/{id}/tags")
    public Result<List<PetTagResponse>> deletePetTags(@PathVariable("id") Long petId, @RequestBody IdsRequest request) {
        List<PetTagResponse> response = petService.deleteTags(petId, request);
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
        Page<PetStatusRecord> page = DbUtils.createPage(currentPage, size, sort, order);
        Page<PetStatusRecordResponse> response = petService.getStatusRecords(petId, page, Set.of());
        return Result.success(response);
    }
}
