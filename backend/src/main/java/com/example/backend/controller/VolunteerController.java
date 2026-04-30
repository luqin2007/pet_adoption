package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.service.VolunteerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 志愿者管理模块<br>
 * - 志愿者招募<br>
 * ---- 创建招募计划 addRecruitment ( √ × )<br>
 * ---- 查询招募计划 getRecruitments ( √ × )<br>
 * ---- 获取招募计划 getRecruitment ( √ × )<br>
 * ---- 修改招募计划 updateRecruitment ( √ × )<br>
 * ---- 设置招募状态 updateRecruitmentStatus ( √ × )<br>
 * - 志愿者申请<br>
 * ---- 提交申请 addApplication ( √ × )<br>
 * ---- 查询申请列表 getApplications ( √ × )<br>
 * ---- 获取申请详情 getApplication ( √ × )<br>
 * ---- 更新申请状态 setApplicationStatus ( √ × )<br>
 * - 志愿者档案<br>
 * ---- 查询档案 getProfiles ( √ × )<br>
 * ---- 获取档案 getProfile ( √ × )<br>
 * ---- 修改档案 updateProfile ( √ × )<br>
 * ---- 设置档案状态 updateProfileStatus ( √ × )<br>
 * - 志愿者排班<br>
 * ---- 创建排班 addShift ( √ × )<br>
 * ---- 查询排班 getShifts ( √ × )<br>
 * ---- 获取排班 getShift ( √ × )<br>
 * ---- 修改排班 updateShift ( √ × )<br>
 * ---- 修改排班状态 updateShiftStatus ( √ × )<br>
 * - 服务记录<br>
 * ---- 提交服务记录 addServiceRecord ( √ × )<br>
 * ---- 查询服务记录 getServiceRecords ( √ × )<br>
 * ---- 获取服务记录 getServiceRecord ( √ × )<br>
 * ---- 修改服务记录状态 updateServiceRecordStatus ( √ × )<br>
 * ---- 审核驳回 rejectServiceRecord ( √ × )<br>
 * - 志愿者激励<br>
 * ---- 创建激励 addReward ( √ × )<br>
 * ---- 查询激励 getRewards ( √ × )<br>
 * ---- 获取激励详情 getReward ( √ × )<br>
 * ---- 发放激励 issueReward ( √ × )<br>
 */
@Validated
@RestController
@RequestMapping("/api/v1/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    /**
     * 创建招募计划
     */
    @PostMapping("/recruitments")
    public Result<VolunteerRecruitmentResponse> addRecruitment(@Valid @RequestBody VolunteerRecruitmentAddRequest request) {
        return Result.success(volunteerService.addRecruitment(request));
    }

    /**
     * 获取招募计划
     */
    @GetMapping("/recruitments/{id}")
    public Result<VolunteerRecruitmentResponse> getRecruitment(@PathVariable("id") Long recruitmentId) {
        return Result.success(volunteerService.getRecruitment(recruitmentId));
    }

    /**
     * 查询招募计划
     */
    @GetMapping("/recruitments")
    public Result<Page<VolunteerRecruitmentResponse>> getRecruitments(VolunteerRecruitmentQueryParams query, PageParams page) {
        return Result.success(volunteerService.getRecruitments(query, page));
    }

    /**
     * 修改招募计划
     */
    @PutMapping("/recruitments/{id}")
    public Result<VolunteerRecruitmentResponse> updateRecruitment(@PathVariable("id") Long recruitmentId,
                                                                  @Valid @RequestBody VolunteerRecruitmentUpdateRequest request) {
        return Result.success(volunteerService.updateRecruitment(recruitmentId, request));
    }

    /**
     * 修改招募计划状态
     */
    @PatchMapping("/recruitments/{id}")
    public Result<VolunteerRecruitmentResponse> updateRecruitmentStatus(@PathVariable("id") Long recruitmentId,
                                                                        @Valid @RequestBody VolunteerRecruitmentStatusUpdateRequest request) {
        return Result.success(volunteerService.updateRecruitmentStatus(recruitmentId, request));
    }

    /**
     * 提交志愿者申请
     */
    @PostMapping("/applications")
    public Result<VolunteerApplicationResponse> addApplication(@Valid @RequestBody VolunteerApplicationAddRequest request) {
        return Result.success(volunteerService.addApplication(request));
    }

    /**
     * 查询申请列表
     */
    @GetMapping("/applications")
    public Result<Page<VolunteerApplicationResponse>> getApplications(VolunteerApplicationQueryParams query, PageParams page) {
        return Result.success(volunteerService.getApplications(query, page));
    }

    /**
     * 获取申请详情
     */
    @GetMapping("/applications/{id}")
    public Result<VolunteerApplicationResponse> getApplication(@PathVariable("id") Long applicationId) {
        return Result.success(volunteerService.getApplication(applicationId));
    }

    /**
     * 更新申请状态
     */
    @PatchMapping("/applications/{id}")
    public Result<VolunteerApplicationResponse> setApplicationStatus(@PathVariable("id") Long applicationId,
                                                                     @Valid @RequestBody VolunteerApplicationStatusUpdateRequest request) {
        return Result.success(volunteerService.setApplicationStatus(applicationId, defaultApplicationReview(request)));
    }

    /**
     * 查询志愿者档案
     */
    @GetMapping("/profiles")
    public Result<Page<VolunteerProfileResponse>> getProfiles(VolunteerProfileQueryParams query, PageParams page) {
        return Result.success(volunteerService.getProfiles(query, page));
    }

    /**
     * 获取志愿者档案
     */
    @GetMapping("/profiles/{id}")
    public Result<VolunteerProfileResponse> getProfile(@PathVariable("id") Long profileId) {
        return Result.success(volunteerService.getProfile(profileId));
    }

    /**
     * 修改志愿者档案
     */
    @PutMapping("/profiles/{id}")
    public Result<VolunteerProfileResponse> updateProfile(@PathVariable("id") Long profileId,
                                                          @Valid @RequestBody VolunteerProfileUpdateRequest request) {
        return Result.success(volunteerService.updateProfile(profileId, request));
    }

    /**
     * 修改志愿者档案状态
     */
    @PostMapping("/profiles/{id}/{st}")
    public Result<VolunteerProfileResponse> updateProfileStatus(@PathVariable("id") Long profileId,
                                                                @PathVariable("st") String status) {
        return Result.success(volunteerService.updateProfileStatus(profileId, status));
    }

    /**
     * 创建排班
     */
    @PostMapping("/shifts")
    public Result<VolunteerShiftResponse> addShift(@Valid @RequestBody VolunteerShiftAddRequest request) {
        return Result.success(volunteerService.addShift(request));
    }

    /**
     * 查询排班
     */
    @GetMapping("/shifts")
    public Result<Page<VolunteerShiftResponse>> getShifts(VolunteerShiftQueryParams query, PageParams page) {
        return Result.success(volunteerService.getShifts(query, page));
    }

    /**
     * 获取排班详情
     */
    @GetMapping("/shifts/{id}")
    public Result<VolunteerShiftResponse> getShift(@PathVariable("id") Long shiftId) {
        return Result.success(volunteerService.getShift(shiftId));
    }

    /**
     * 修改排班
     */
    @PutMapping("/shifts/{id}")
    public Result<VolunteerShiftResponse> updateShift(@PathVariable("id") Long shiftId,
                                                      @Valid @RequestBody VolunteerShiftUpdateRequest request) {
        return Result.success(volunteerService.updateShift(shiftId, request));
    }

    /**
     * 修改排班状态
     */
    @PatchMapping("/shifts/{id}")
    public Result<VolunteerShiftResponse> updateShiftStatus(@PathVariable("id") Long shiftId,
                                                            @Valid @RequestBody VolunteerShiftStatusUpdateRequest request) {
        return Result.success(volunteerService.updateShiftStatus(shiftId, request));
    }

    /**
     * 提交服务记录
     */
    @PostMapping("/shifts/{id}/records")
    public Result<VolunteerServiceRecordResponse> addServiceRecord(@PathVariable("id") Long shiftId,
                                                                   @Valid @RequestBody VolunteerServiceRecordAddRequest request) {
        return Result.success(volunteerService.addServiceRecord(shiftId, request));
    }

    /**
     * 查询服务记录
     */
    @GetMapping("/records")
    public Result<Page<VolunteerServiceRecordResponse>> getServiceRecords(VolunteerServiceRecordQueryParams query, PageParams page) {
        return Result.success(volunteerService.getServiceRecords(query, page));
    }

    /**
     * 审核服务记录
     */
    @PatchMapping("/records/{id}")
    public Result<VolunteerServiceRecordResponse> updateServiceRecordStatus(@PathVariable("id") Long recordId,
                                                                            @Valid @RequestBody VolunteerRecordReviewRequest request) {
        return Result.success(volunteerService.updateServiceRecordStatus(recordId, request));
    }

    /**
     * 获取服务记录详情
     */
    @GetMapping("/records/{id}")
    public Result<VolunteerServiceRecordResponse> getServiceRecord(@PathVariable("id") Long recordId) {
        return Result.success(volunteerService.getServiceRecord(recordId));
    }

    /**
     * 创建激励记录
     */
    @PostMapping("/rewards")
    public Result<VolunteerRewardResponse> addReward(@Valid @RequestBody VolunteerRewardAddRequest request) {
        return Result.success(volunteerService.addReward(request));
    }

    /**
     * 查询激励记录
     */
    @GetMapping("/rewards")
    public Result<Page<VolunteerRewardResponse>> getRewards(VolunteerRewardQueryParams query, PageParams page) {
        return Result.success(volunteerService.getRewards(query, page));
    }

    /**
     * 获取激励详情
     */
    @GetMapping("/rewards/{id}")
    public Result<VolunteerRewardResponse> getReward(@PathVariable("id") Long rewardId) {
        return Result.success(volunteerService.getReward(rewardId));
    }

    /**
     * 发放激励
     */
    @PostMapping("/rewards/{id}/issue")
    public Result<VolunteerRewardResponse> issueReward(@PathVariable("id") Long rewardId) {
        return Result.success(volunteerService.issueReward(rewardId));
    }

    private VolunteerApplicationStatusUpdateRequest defaultApplicationReview(VolunteerApplicationStatusUpdateRequest request) {
        return request == null ? new VolunteerApplicationStatusUpdateRequest() : request;
    }

    private VolunteerRecordReviewRequest defaultRecordReview(VolunteerRecordReviewRequest request) {
        return request == null ? new VolunteerRecordReviewRequest() : request;
    }
}
