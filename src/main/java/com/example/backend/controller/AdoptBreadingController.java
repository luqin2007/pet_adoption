package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.service.AdoptBreadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 领养与寄养管理模块<br>
 * - 申请发起 ( √ × )<br>
 * ---- 申请领养 addAdopt ( √ × )<br>
 * ---- 申请寄养 addBreading ( √ × )<br>
 * - 申请审核 ( √ × )<br>
 * ---- 获取领养申请 getAdoptApplication ( √ × )<br>
 * ---- 查找领养申请 getAdoptApplications ( √ × )<br>
 * ---- 更新领养状态 updateAdoptApplicationStatus ( √ × )<br>
 * ---- 获取寄养申请 getBreadingApplication ( √ × )<br>
 * ---- 查找寄养申请 getBreadingApplications ( √ × )<br>
 * ---- 更新寄养状态 updateBreadingApplicationStatus ( √ × )<br>
 * - 协议签订 ( √ × )<br>
 * ---- 起草协议 addAgreement ( √ × )<br>
 * ---- 修改协议 updateAgreement ( √ × )<br>
 * ---- 上传协议扫描件 uploadAgreement ( √ × )<br>
 * ---- 协议签订 signAgreement ( √ × )<br>
 * ---- 获取协议 getAgreement ( √ × )<br>
 * ---- 查找协议 getAgreements ( √ × )<br>
 * - 后续跟踪 ( √ × )<br>
 * ---- 创建跟踪任务 addFollowTask ( √ × )<br>
 * ---- 更新跟踪任务 updateFollowTask ( √ × )<br>
 * ---- 查询跟踪任务 getFollowTasks ( √ × )<br>
 * ---- 获取跟踪任务 getFollowTask ( √ × )<br>
 * ---- 提交跟踪记录 addFollowRecord ( √ × )<br>
 * ---- 获取跟踪记录 getFollowRecords ( √ × )<br>
 * ---- 观察寄养宠物 observeBreadingPet ( TODO × )
 */
@Validated
@RestController
@RequestMapping("/adopt")
@RequiredArgsConstructor
public class AdoptBreadingController {

    private final AdoptBreadingService adoptBreadingService;

    /**
     * 申请领养
     */
    @PostMapping("/adopt")
    public Result<AdoptResponse> addAdopt(@RequestBody AdoptAddRequest request) {
        AdoptResponse response = adoptBreadingService.addAdopt(request);
        return Result.success(response);
    }

    /**
     * 获取领养申请
     */
    @GetMapping("/adopt/{id}")
    public Result<AdoptResponse> getAdopt(@PathVariable("id") Long adoptId) {
        AdoptResponse response = adoptBreadingService.getAdopt(adoptId);
        return Result.success(response);
    }

    /**
     * 获取领养申请
     */
    @GetMapping("/adopt")
    public Result<Page<AdoptResponse>> getAdopts(AdoptQueryParams params, PageParams page) {
        Page<AdoptResponse> response = adoptBreadingService.getAdopts(params, page);
        return Result.success(response);
    }

    /**
     * 申请领养审核
     */
    @PostMapping("/adopt/{id}/{st}")
    public Result<AdoptResponse> updateAdoptStatus(@PathVariable("id") Long adoptId,
                                                   @PathVariable("st") String status) {
        AdoptResponse response = adoptBreadingService.updateAdoptStatus(adoptId, status);
        return Result.success(response);
    }

    /**
     * 申请寄养
     */
    @PostMapping("/breading")
    public Result<BreadingResponse> addBreading(@RequestBody BreadingAddRequest request) {
        BreadingResponse response = adoptBreadingService.addBreading(request);
        return Result.success(response);
    }

    /**
     * 获取寄养申请
     */
    @GetMapping("/breading/{id}")
    public Result<BreadingResponse> getBreading(@PathVariable("id") Long breadingId) {
        BreadingResponse response = adoptBreadingService.getBreading(breadingId);
        return Result.success(response);
    }

    /**
     * 获取寄养申请
     */
    @GetMapping("/breading")
    public Result<Page<BreadingResponse>> getBreadingPets(BreadingQueryParams params, PageParams page) {
        Page<BreadingResponse> response = adoptBreadingService.getBreadingPets(params, page);
        return Result.success(response);
    }

    /**
     * 更新寄养审核状态
     */
    @PostMapping("/breading/{id}/{st}")
    public Result<BreadingResponse> updateBreadingStatus(@PathVariable("id") Long breadingId,
                                                         @PathVariable("st") String status) {
        BreadingResponse response = adoptBreadingService.updateBreadingStatus(breadingId, status);
        return Result.success(response);
    }

    /**
     * 起草协议
     */
    @PostMapping("/agreement")
    public Result<AgreementResponse> addAgreement(@RequestBody AgreementAddRequest request) {
        AgreementResponse response = adoptBreadingService.addAgreement(request);
        return Result.success(response);
    }

    /**
     * 修改协议
     */
    @PostMapping("/agreement/{id}")
    public Result<AgreementResponse> updateAgreement(@PathVariable("id") Long agreementId,
                                                     @RequestBody AgreementUpdateRequest request) {
        AgreementResponse response = adoptBreadingService.updateAgreement(agreementId, request);
        return Result.success(response);
    }

    /**
     * 上传协议扫描件
     */
    @PostMapping("/agreement/{id}/files")
    public Result<List<AgreementFileResponse>> uploadAgreement(@PathVariable("id") Long agreementId,
                                                               AgreementFilesUploadTable files) {
        List<AgreementFileResponse> response = adoptBreadingService.uploadAgreement(agreementId, files);
        return Result.success(response);
    }

    /**
     * 签署协议
     */
    @PostMapping("/agreement/{id}/sign")
    public Result<AgreementResponse> signAgreement(@PathVariable("id") Long agreementId,
                                                   @RequestParam("sign") MultipartFile sign) {
        AgreementResponse response = adoptBreadingService.signAgreement(agreementId, sign);
        return Result.success(response);
    }

    /**
     * 获取协议
     */
    @GetMapping("/agreement/{id}")
    public Result<AgreementResponse> getAgreement(@PathVariable("id") Long agreementId) {
        AgreementResponse response = adoptBreadingService.getAgreement(agreementId);
        return Result.success(response);
    }

    /**
     * 查找协议
     */
    @GetMapping("/agreement")
    public Result<Page<AgreementResponse>> getAgreements(AgreementQueryParams query, PageParams page) {
        Page<AgreementResponse> response = adoptBreadingService.getAgreements(query, page);
        return Result.success(response);
    }

    /**
     * 创建跟踪任务
     */
    @PostMapping("/follow/adopt/{id}")
    public Result<FollowTaskResponse> addFollowTask(@PathVariable("id") Long applicationId,
                                                    @RequestBody FollowTaskAddRequest request) {
        FollowTaskResponse response = adoptBreadingService.addFollowTask(applicationId, request);
        return Result.success(response);
    }

    /**
     * 更新跟踪任务
     */
    @PostMapping("/follow/{id}")
    public Result<FollowTaskResponse> updateFollowTask(@PathVariable("id") Long taskId,
                                                       @RequestBody FollowTaskUpdateRequest request) {
        FollowTaskResponse response = adoptBreadingService.updateFollowTask(taskId, request);
        return Result.success(response);
    }

    /**
     * 获取跟踪任务
     */
    @GetMapping("/follow/{id}")
    public Result<FollowTaskResponse> getFollowTask(@PathVariable("id") Long taskId) {
        FollowTaskResponse response = adoptBreadingService.getFollowTask(taskId);
        return Result.success(response);
    }

    /**
     * 查询跟踪任务
     */
    @GetMapping("/follow")
    public Result<Page<FollowTaskResponse>> getFollowTasks(FollowTaskQueryParams query, PageParams page) {
        Page<FollowTaskResponse> response = adoptBreadingService.getFollowTasks(query, page);
        return Result.success(response);
    }

    /**
     * 提交跟踪记录
     */
    @PostMapping("/follow/{id}/record")
    public Result<FollowRecordResponse> addFollowRecord(@PathVariable("id") Long taskId, @RequestBody FollowRecordAddRequest request) {
        FollowRecordResponse response = adoptBreadingService.addFollowRecord(taskId, request);
        return Result.success(response);
    }

    /**
     * 获取跟踪记录
     */
    @GetMapping("/follow/{id}/record")
    public Result<Page<FollowRecordResponse>> getFollowRecords(@PathVariable("id") Long taskId, PageParams page) {
        Page<FollowRecordResponse> response = adoptBreadingService.getFollowRecords(taskId, page);
        return Result.success(response);
    }

    /**
     * 查询跟踪记录
     */
    @GetMapping("/follow/record")
    public Result<Page<FollowRecordResponse>> getFollowRecords(FollowRecordQueryParams query, PageParams page) {
        Page<FollowRecordResponse> response = adoptBreadingService.getFollowRecords(query, page);
        return Result.success(response);
    }
}
