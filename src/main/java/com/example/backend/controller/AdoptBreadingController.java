package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.PageParams;
import com.example.backend.dto.Result;
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

    /**
     * 申请领养
     */
    @PostMapping("/adopt")
    public Result<AdoptResponse> addAdopt(@RequestBody AdoptRequest request) {
    }

    /**
     * 获取领养申请
     */
    @GetMapping("/adopt/{id}")
    public Result<AdoptResponse> getAdoptApplication(@PathVariable("id") Long adoptId) {
    }

    /**
     * 获取领养申请
     */
    @GetMapping("/adopt")
    public Result<AdoptResponse> getAdoptApplications(AdoptQueryParams params, PageParams page) {
    }

    /**
     * 申请领养审核
     */
    @PostMapping("/adopt/{id}/{st}")
    public Result<AdoptResponse> reviewAdoptApplication(@PathVariable("id") Long adoptId,
                                                        @PathVariable("st") String status) {
    }

    /**
     * 申请寄养
     */
    @PostMapping("/breading")
    public Result<BreadingResponse> addBreading(@RequestBody BreadingRequest request) {
    }

    /**
     * 获取寄养申请
     */
    @GetMapping("/breading/{id}")
    public Result<BreadingResponse> getBreadingApplication(@PathVariable("id") Long breadingId) {
    }

    /**
     * 获取寄养申请
     */
    @GetMapping("/breading")
    public Result<Page<BreadingResponse>> getBreadingApplications(BreadingQueryParams params, PageParams page) {
    }

    /**
     * 更新寄养审核状态
     */
    @PostMapping("/breading/{id}/{st}")
    public Result<BreadingResponse> reviewBreadingApplication(@PathVariable("id") Long breadingId,
                                                              @PathVariable("st") String status) {
    }

    /**
     * 起草协议
     */
    @PostMapping("/agreement")
    public Result<AgreementResponse> addAgreement(@RequestBody AgreementAddRequest request) {
    }

    /**
     * 修改协议
     */
    @PostMapping("/agreement/{id}")
    public Result<AgreementResponse> updateAgreement(@PathVariable("id") Long agreementId,
                                                     @RequestBody AgreementUpdateRequest request) {
    }

    /**
     * 上传协议扫描件
     */
    @PostMapping("/agreement/{id}")
    public Result<List<String>> uploadAgreement(@PathVariable("id") Long agreementId, List<MultipartFile> files) {
    }

    /**
     * 签署协议
     */
    @PostMapping("/agreement/{id}/sign")
    public Result<AgreementResponse> signAgreement(@PathVariable("id") Long agreementId, MultipartFile sign) {
    }

    /**
     * 获取协议
     */
    @GetMapping("/agreement/{id}")
    public Result<AgreementResponse> getAgreement(@PathVariable("id") Long agreementId) {
    }

    /**
     * 查找协议
     */
    @GetMapping("/agreement")
    public Result<Page<AgreementResponse>> getAgreements(AgreementQueryParams query, PageParams page) {
    }

    /**
     * 创建跟踪任务
     */
    @PostMapping("/follow/adopt/{id}")
    public Result<FollowTaskResponse> addFollowTask(@PathVariable("id") Long applicationId,
                                                    @RequestBody FollowTaskAddRequest request) {
    }

    /**
     * 更新跟踪任务
     */
    @PostMapping("/follow/{id}")
    public Result<FollowTaskResponse> updateFollowTask(@PathVariable("id") Long taskId,
                                                       @RequestBody FollowTaskUpdateRequest request) {
    }

    /**
     * 获取跟踪任务
     */
    @GetMapping("/follow/{id}")
    public Result<FollowTaskResponse> getFollowTask(@PathVariable("id") Long taskId) {
    }

    /**
     * 查询跟踪任务
     */
    @GetMapping("/follow")
    public Result<Page<FollowTaskResponse>> getFollowTasks(FollowTaskQueryParams query, PageParams page) {
    }

    /**
     * 提交跟踪记录
     */
    @PostMapping("/follow/record")
    public Result<FollowRecordResponse> addFollowRecord(@RequestBody FollowRecordAddRequest request) {
    }

    /**
     * 获取跟踪记录
     */
    @GetMapping("/follow/record/{id}")
    public Result<Page<FollowRecordResponse>> getFollowRecords(@PathVariable("id") Long taskId) {
    }

    /**
     * 查询跟踪记录
     */
    @GetMapping("/follow/record")
    public Result<Page<FollowRecordResponse>> getFollowRecords(FollowRecordQueryParams query, PageParams page) {
    }
}

