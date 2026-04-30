package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.service.LostPetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 走失宠物报备模块<br>
 * - 走失报备 ( √ × )<br>
 * ---- 准备报备走失：beginLostPet ( √ × )<br>
 * ---- 报备走失宠物：addLostPet ( √ × )<br>
 * ---- 上传宠物图片：uploadLostPetMedia ( √ × )<br>
 * ---- 删除宠物图片：deleteLostPetMedia ( √ × )<br>
 * ---- 获取走失宠物：getLostPet ( √ × )<br>
 * ---- 查询走失宠物：getLostPets ( √ × )<br>
 * ---- 修改走失宠物：updateLostPet ( √ × )<br>
 * ---- 查看相似流浪宠物：getSimilarPets ( √ × )<br>
 * ---- 标记宠物不是自己丢失的宠物：markPetMismatch ( √ × )<br>
 * - 认领申请 ( √ × )<br>
 * ---- 发起认领申请：addClaim ( √ × )<br>
 * ---- 获取认领申请：getClaim ( √ × )<br>
 * - 认领审核 ( √ × )<br>
 * ---- 认领申请审核：approveClaim ( √ × )<br>
 * - 认领记录 ( √ × )<br>
 * ---- 查询认领申请：getClaims ( √ × )<br>
 * ---- 取消认领申请：cancelClaim ( √ × )
 */
@Validated
@RestController
@RequestMapping("/api/v1/lost")
@RequiredArgsConstructor
public class LostPetController {

    private final LostPetService lostPetService;

    /**
     * 准备报备走失
     */
    @PutMapping("/pets")
    public Result<String> beginLostPet() {
        String response = lostPetService.beginLostPet();
        return Result.success(response);
    }

    /**
     * 报备走失宠物
     */
    @PostMapping("/pets")
    public Result<LostPetResponse> addLostPet(@Valid @RequestBody LostPetAddRequest request) {
        LostPetResponse response = lostPetService.addLostPet(request);
        return Result.success(response);
    }

    /**
     * 上传宠物图片
     */
    @PutMapping("/pets/{_id}/media")
    public Result<String> uploadLostPetMedia(@PathVariable("_id") String uuid,
                                             @ModelAttribute LostPetMediaUploadTable request) {
        String response = lostPetService.uploadLostPetMedia(uuid, request);
        return Result.success(response);
    }

    /**
     * 删除宠物图片
     */
    @DeleteMapping("/pets/{_id}/media/{name}")
    public Result<Void> deleteLostPetMedia(@PathVariable("_id") String uuid, @PathVariable("name") String filename) {
        lostPetService.deleteLostPetMedia(uuid, filename);
        return Result.success();
    }

    /**
     * 修改走失宠物
     */
    @PutMapping("/pets/{id}")
    public Result<LostPetResponse> updateLostPet(@PathVariable("id") Long lostPetId,
                                                 @Valid @RequestBody LostPetUpdateRequest request) {
        LostPetResponse response = lostPetService.updateLostPet(lostPetId, request);
        return Result.success(response);
    }

    /**
     * 修改走失宠物状态
     */
    @PatchMapping("/pets/{id}/status")
    public Result<LostPetResponse> updateLostPetStatus(@PathVariable("id") Long lostPetId,
                                                       @Valid @RequestBody LostPetStatusUpdateRequest request) {
        LostPetResponse response = lostPetService.updateLostPetStatus(lostPetId, request);
        return Result.success(response);
    }

    /**
     * 查看相似流浪宠物
     */
    @GetMapping("/pets/{id}/similar")
    public Result<List<PetResponse>> getSimilarPets(@PathVariable("id") Long lostPetId) {
        List<PetResponse> response = lostPetService.getSimilarPets(lostPetId);
        return Result.success(response);
    }

    /**
     * 标记某不是丢失的宠物
     */
    @PostMapping("/pets/{id}/mismatch/{pid}")
    public Result<List<PetResponse>> markPetMismatch(@PathVariable("id") Long lostPetId,
                                                     @PathVariable("pid") Long petId) {
        List<PetResponse> response = lostPetService.markPetMismatch(lostPetId, petId);
        return Result.success(response);
    }

    /**
     * 获取走失宠物
     */
    @GetMapping("/pets/{id}")
    public Result<LostPetResponse> getLostPet(@PathVariable("id") Long petId) {
        LostPetResponse response = lostPetService.getLostPet(petId);
        return Result.success(response);
    }

    /**
     * 查询走失宠物
     */
    @GetMapping("/pets")
    public Result<Page<LostPetResponse>> getLostPets(@Valid LostPetQueryParams params, PageParams pageParams) {
        Page<LostPetResponse> response = lostPetService.getLostPets(params, pageParams);
        return Result.success(response);
    }

    /**
     * 发起认领申请
     */
    @PostMapping("/claim")
    public Result<LostPetClaimResponse> addClaim(@Valid @RequestBody LostPetClaimAddRequest request) {
        LostPetClaimResponse response = lostPetService.addClaim(request);
        return Result.success(response);
    }

    /**
     * 获取认领申请
     */
    @GetMapping("/claim/{id}")
    public Result<LostPetClaimResponse> getClaim(@PathVariable("id") Long claimId) {
        LostPetClaimResponse response = lostPetService.getClaim(claimId);
        return Result.success(response);
    }

    /**
     * 查询认领申请
     */
    @GetMapping("/claim")
    public Result<Page<LostPetClaimResponse>> getClaims(@Valid ClaimQueryParams params, PageParams pageParams) {
        Page<LostPetClaimResponse> response = lostPetService.getClaims(params, pageParams);
        return Result.success(response);
    }

    /**
     * 取消认领申请
     */
    @DeleteMapping("/claim/{id}")
    public Result<Void> cancelClaim(@PathVariable("id") Long claimId) {
        lostPetService.cancelClaim(claimId);
        return Result.success();
    }

    /**
     * 认领申请审核
     */
    @PatchMapping("/claim/{id}/approve")
    public Result<LostPetClaimResponse> approveClaim(@PathVariable("id") Long claimId,
                                                     @Valid @RequestBody ClaimApproveRequest request) {
        LostPetClaimResponse response = lostPetService.approveClaim(claimId, request);
        return Result.success(response);
    }
}
