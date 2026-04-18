package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.service.AdoptBreadingService;
import jakarta.validation.Valid;
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
 * ---- 准备起草协议 beginAgreement ( √ × )<br>
 * ---- 上传协议图片（起草阶段） uploadAgreementWhenAdd ( √ × )<br>
 * ---- 删除协议图片（起草阶段） deleteAgreementWhenAdd ( √ × )<br>
 * ---- 起草协议 addAgreement ( √ × )<br>
 * ---- 修改协议 updateAgreement ( √ × )<br>
 * ---- 上传协议图片（已有协议） uploadAgreement ( √ × )<br>
 * ---- 删除协议图片（已有协议） deleteAgreementFile ( √ × )<br>
 * ---- 调整协议顺序（已有协议） reorderAgreementFiles ( √ × )<br>
 * ---- 协议签订 signAgreement ( √ × )<br>
 * ---- 获取协议 getAgreement ( √ × )<br>
 * ---- 查找协议 getAgreements ( √ × )<br>
 * - 后续跟踪 ( √ × )<br>
 * ---- 创建回访任务 addFollowTask ( √ × )<br>
 * ---- 更新回访任务 updateFollowTask ( √ × )<br>
 * ---- 查询回访任务 getFollowTasks ( √ × )<br>
 * ---- 获取回访任务 getFollowTask ( √ × )<br>
 * ---- 提交回访记录 addFollowRecord ( √ × )<br>
 * ---- 获取回访记录 getFollowRecords ( √ × )<br>
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
    public Result<AdoptResponse> addAdopt(@Valid @RequestBody AdoptAddRequest request) {
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
    public Result<Page<AdoptResponse>> getAdopts(@Valid AdoptQueryParams params, PageParams page) {
        Page<AdoptResponse> response = adoptBreadingService.getAdopts(params, page);
        return Result.success(response);
    }

    /**
     * 申请领养审核
     */
    @PatchMapping("/adopt/{id}/{st}")
    public Result<AdoptResponse> updateAdoptStatus(@PathVariable("id") Long adoptId,
                                                   @PathVariable("st") String status) {
        AdoptResponse response = adoptBreadingService.updateAdoptStatus(adoptId, status);
        return Result.success(response);
    }

    /**
     * 申请寄养
     */
    @PostMapping("/breading")
    public Result<BreadingResponse> addBreading(@Valid @RequestBody BreadingAddRequest request) {
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
    public Result<Page<BreadingResponse>> getBreadingPets(@Valid BreadingQueryParams params, PageParams page) {
        Page<BreadingResponse> response = adoptBreadingService.getBreadingPets(params, page);
        return Result.success(response);
    }

    /**
     * 更新寄养审核状态
     */
    @PatchMapping("/breading/{id}/{st}")
    public Result<BreadingResponse> updateBreadingStatus(@PathVariable("id") Long breadingId,
                                                         @PathVariable("st") String status) {
        BreadingResponse response = adoptBreadingService.updateBreadingStatus(breadingId, status);
        return Result.success(response);
    }

    /**
     * 准备起草协议
     */
    @PutMapping("/agreement")
    public Result<String> beginAgreement() {
        String uuid = adoptBreadingService.beginAgreement();
        return Result.success(uuid);
    }

    /**
     * 起草阶段上传协议扫描件
     */
    @PostMapping("/agreement/upload/{_id}")
    public Result<String> uploadAgreementWhenAdd(@PathVariable("_id") String uuid,
                                                 @RequestParam("file") MultipartFile file) {
        String response = adoptBreadingService.uploadAgreementFile(uuid, file);
        return Result.success(response);
    }

    /**
     * 起草阶段删除协议扫描件
     */
    @DeleteMapping("/agreement/upload/{_id}/{name}")
    public Result<Void> deleteAgreementWhenAdd(@PathVariable("_id") String uuid,
                                               @PathVariable("name") String filename) {
        adoptBreadingService.deleteAgreementFile(uuid, filename);
        return Result.success();
    }

    /**
     * 起草协议
     */
    @PostMapping("/agreement")
    public Result<AgreementResponse> addAgreement(@Valid @RequestBody AgreementAddRequest request) {
        AgreementResponse response = adoptBreadingService.addAgreement(request);
        return Result.success(response);
    }

    /**
     * 修改协议
     */
    @PutMapping("/agreement/{id}")
    public Result<AgreementResponse> updateAgreement(@PathVariable("id") Long agreementId,
                                                     @Valid @RequestBody AgreementUpdateRequest request) {
        AgreementResponse response = adoptBreadingService.updateAgreement(agreementId, request);
        return Result.success(response);
    }

    /**
     * 上传协议图片
     */
    @PostMapping("/agreement/{id}/files")
    public Result<List<AgreementFileResponse>> uploadAgreement(@PathVariable("id") Long agreementId,
                                                               @Valid @ModelAttribute AgreementFilesUploadTable files) {
        List<AgreementFileResponse> response = adoptBreadingService.uploadAgreement(agreementId, files);
        return Result.success(response);
    }

    /**
     * 删除协议图片
     */
    @DeleteMapping("/agreement/{id}/files/{fid}")
    public Result<List<AgreementFileResponse>> deleteAgreementFile(@PathVariable("id") Long agreementId,
                                                                   @PathVariable("fid") Long fileId) {
        List<AgreementFileResponse> response = adoptBreadingService.deleteAgreementFile(agreementId, fileId);
        return Result.success(response);
    }

    /**
     * 调整协议顺序
     */
    @PutMapping("/agreement/{id}/files/order")
    public Result<List<AgreementFileResponse>> reorderAgreementFiles(@PathVariable("id") Long agreementId,
                                                                     @Valid @RequestBody AgreementFilesOrderRequest request) {
        List<AgreementFileResponse> response = adoptBreadingService.reorderAgreementFiles(agreementId, request);
        return Result.success(response);
    }

    /**
     * 签署协议
     */
    @PatchMapping("/agreement/{id}/sign")
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
    public Result<Page<AgreementResponse>> getAgreements(@Valid AgreementQueryParams query, PageParams page) {
        Page<AgreementResponse> response = adoptBreadingService.getAgreements(query, page);
        return Result.success(response);
    }

    /**
     * 创建回访任务
     */
    @PostMapping("/follow/adopt/{id}")
    public Result<FollowTaskResponse> addFollowTask(@PathVariable("id") Long applicationId,
                                                    @Valid @RequestBody FollowTaskAddRequest request) {
        FollowTaskResponse response = adoptBreadingService.addFollowTask(applicationId, request);
        return Result.success(response);
    }

    /**
     * 更新回访任务
     */
    @PutMapping("/follow/{id}")
    public Result<FollowTaskResponse> updateFollowTask(@PathVariable("id") Long taskId,
                                                       @Valid @RequestBody FollowTaskUpdateRequest request) {
        FollowTaskResponse response = adoptBreadingService.updateFollowTask(taskId, request);
        return Result.success(response);
    }

    /**
     * 获取回访任务
     */
    @GetMapping("/follow/{id}")
    public Result<FollowTaskResponse> getFollowTask(@PathVariable("id") Long taskId) {
        FollowTaskResponse response = adoptBreadingService.getFollowTask(taskId);
        return Result.success(response);
    }

    /**
     * 查询回访任务
     */
    @GetMapping("/follow")
    public Result<Page<FollowTaskResponse>> getFollowTasks(@Valid FollowTaskQueryParams query, PageParams page) {
        Page<FollowTaskResponse> response = adoptBreadingService.getFollowTasks(query, page);
        return Result.success(response);
    }

    /**
     * 提交回访记录
     */
    @PostMapping("/follow/{id}/record")
    public Result<FollowRecordResponse> addFollowRecord(@PathVariable("id") Long taskId,
                                                        @Valid @RequestBody FollowRecordAddRequest request) {
        FollowRecordResponse response = adoptBreadingService.addFollowRecord(taskId, request);
        return Result.success(response);
    }

    /**
     * 获取回访记录
     */
    @GetMapping("/follow/{id}/record")
    public Result<Page<FollowRecordResponse>> getFollowRecords(@PathVariable("id") Long taskId, PageParams page) {
        Page<FollowRecordResponse> response = adoptBreadingService.getFollowRecords(taskId, page);
        return Result.success(response);
    }

    /**
     * 查询回访记录
     */
    @GetMapping("/follow/record")
    public Result<Page<FollowRecordResponse>> getFollowRecords(@Valid FollowRecordQueryParams query, PageParams page) {
        Page<FollowRecordResponse> response = adoptBreadingService.getFollowRecords(query, page);
        return Result.success(response);
    }
}
