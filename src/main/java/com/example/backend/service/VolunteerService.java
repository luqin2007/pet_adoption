package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.*;
import com.example.backend.event.VolunteerRecordAddEvent;
import com.example.backend.event.VolunteerRecordStatusEvent;
import com.example.backend.event.VolunteerShiftAddEvent;
import com.example.backend.event.VolunteerShiftStatusEvent;
import com.example.backend.facade.VolunteerFacade;
import com.example.backend.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 志愿者管理
 */
@Service
@RequiredArgsConstructor
public class VolunteerService extends BaseService<VolunteerRecruitmentMapper, VolunteerRecruitment> {

    private final VolunteerApplicationMapper volunteerApplicationMapper;
    private final VolunteerProfileMapper volunteerProfileMapper;
    private final VolunteerShiftMapper volunteerShiftMapper;
    private final VolunteerShiftStatusRecordMapper volunteerShiftStatusRecordMapper;
    private final VolunteerServiceRecordMapper volunteerServiceRecordMapper;
    private final VolunteerRewardMapper volunteerRewardMapper;

    private final VolunteerFacade volunteerFacade;

    private UserService userService;

    /**
     * 创建招募计划
     */
    @Transactional
    public VolunteerRecruitmentResponse addRecruitment(VolunteerRecruitmentAddRequest request) {
        User login = requireWorker();
        VolunteerRecruitment recruitment = request.create(login.getId());
        save(recruitment);
        return volunteerFacade.buildRecruitmentResponse(recruitment);
    }

    /**
     * 获取招募计划
     */
    public VolunteerRecruitmentResponse getRecruitment(Long recruitmentId) {
        return volunteerFacade.buildRecruitmentResponse(requireById(recruitmentId));
    }

    /**
     * 查询招募计划
     */
    public Page<VolunteerRecruitmentResponse> getRecruitments(VolunteerRecruitmentQueryParams query, PageParams page) {
        Page<VolunteerRecruitment> result = baseMapper.queryByRequest(query).page(page);
        return volunteerFacade.buildRecruitmentPage(result);
    }

    /**
     * 修改招募计划
     */
    @Transactional
    public VolunteerRecruitmentResponse updateRecruitment(Long recruitmentId, VolunteerRecruitmentUpdateRequest request) {
        requireWorker();
        VolunteerRecruitment recruitment = requireById(recruitmentId);
        require(VolunteerRecruitmentStatus.PUBLISHED != recruitment.getStatus(), "已发布不可修改");
        request.applyTo(recruitment);
        updateById(recruitment);
        return volunteerFacade.buildRecruitmentResponse(recruitment);
    }

    /**
     * 修改招募计划状态
     */
    @Transactional
    public VolunteerRecruitmentResponse updateRecruitmentStatus(Long recruitmentId, String statusName) {
        requireWorker();
        VolunteerRecruitment recruitment = requireById(recruitmentId);
        VolunteerRecruitmentStatus status = VolunteerRecruitmentStatus.get(statusName);
        if (status == VolunteerRecruitmentStatus.PUBLISHED) {
            requireEqual(VolunteerRecruitmentStatus.DRAFT, recruitment.getStatus(), "仅草稿状态可发布");
        }

        recruitment.setStatus(status);
        recruitment.setUpdateTime(new Date());
        updateById(recruitment);
        return volunteerFacade.buildRecruitmentResponse(recruitment);
    }

    /**
     * 提交志愿者申请
     */
    @Transactional
    public VolunteerApplicationResponse addApplication(VolunteerApplicationAddRequest request) {
        User login = requireLoginUser();
        require(!login.isVolunteer(), "您已是志愿者");

        VolunteerRecruitment recruitment = requireById(request.getRecruitmentId());
        assertRecruitmentOpen(recruitment);
        require(!volunteerApplicationMapper.queryByRecruitmentAndUser(recruitment.getId(), login.getId()).exists(), "请勿重复提交申请");

        VolunteerApplication application = request.create(login.getId());
        volunteerApplicationMapper.insert(application);

        int appliedCount = recruitment.getAppliedCount() == null ? 0 : recruitment.getAppliedCount();
        recruitment.setAppliedCount(appliedCount + 1);
        recruitment.setUpdateTime(new Date());
        updateById(recruitment);
        return volunteerFacade.buildApplicationResponse(application);
    }

    /**
     * 获取申请详情
     */
    public VolunteerApplicationResponse getApplication(Long applicationId) {
        VolunteerApplication application = volunteerApplicationMapper.requireById(applicationId);
        User login = requireLoginUser();
        requirePermission(login.isWorker() || login.is(application.getUserId()));
        return volunteerFacade.buildApplicationResponse(application);
    }

    /**
     * 查询申请列表
     */
    public Page<VolunteerApplicationResponse> getApplications(VolunteerApplicationQueryParams query, PageParams page) {
        User login = requireLoginUser();
        if (!login.isWorker()) { // 非管理员只能查看自己的申请
            query.setUser(login.getId());
        }
        Page<VolunteerApplication> result = volunteerApplicationMapper.queryByRequest(query).page(page);
        return volunteerFacade.buildApplicationPage(result);
    }

    /**
     * 审核通过申请
     */
    @Transactional
    public VolunteerApplicationResponse setApplicationStatus(Long applicationId, VolunteerApplicationStatusUpdateRequest request) {
        User login = requireLoginUser();

        VolunteerApplicationStatus status = VolunteerApplicationStatus.get(request.getStatus());
        if (status.requireWorker())
            requireWorker();

        VolunteerApplication application = volunteerApplicationMapper.requireById(applicationId);
        require(status.canSwitchFrom(application.getStatus()), "当前申请状态不可用");

        // 申请状态
        Date now = new Date();
        application.setStatus(status, request.getReason(), login);
        application.setUpdateTime(now);
        volunteerApplicationMapper.updateById(application);

        if (status == VolunteerApplicationStatus.APPROVED) {
            // 更新用户角色
            User applicant = userService.requireById(application.getUserId());
            int role = applicant.getRole() == null ? 0 : applicant.getRole();
            applicant.setRole(role | UserRole.VOLUNTEER.getSetMask());
            userService.updateById(applicant);
            // 更新志愿者档案
            VolunteerProfile profile = volunteerProfileMapper.queryByUserId(application.getUserId()).one();
            if (profile == null) {
                profile = request.createProfile(application, applicant, login);
                volunteerProfileMapper.insert(profile);
            } else {
                request.applyTo(profile, application, login);
                volunteerProfileMapper.updateById(profile);
            }
        }
        return volunteerFacade.buildApplicationResponse(application);
    }

    /**
     * 获取志愿者档案
     */
    public VolunteerProfileResponse getProfile(Long profileId) {
        VolunteerProfile profile = volunteerProfileMapper.requireById(profileId);
        User login = requireLoginUser();
        requirePermission(login.isWorker() || login.is(profile.getUserId()));
        return volunteerFacade.buildProfileResponse(profile);
    }

    /**
     * 查询志愿者档案
     */
    public Page<VolunteerProfileResponse> getProfiles(VolunteerProfileQueryParams query, PageParams page) {
        requireWorker();
        Page<VolunteerProfile> result = volunteerProfileMapper.queryByRequest(query).page(page);
        return volunteerFacade.buildProfilePage(result);
    }

    /**
     * 修改志愿者档案
     */
    @Transactional
    public VolunteerProfileResponse updateProfile(Long profileId, VolunteerProfileUpdateRequest request) {
        VolunteerProfile profile = volunteerProfileMapper.requireById(profileId);
        User login = requireLoginUser();
        requirePermission(login.isWorker() || login.is(profile.getUserId()));
        request.applyTo(profile);
        volunteerProfileMapper.updateById(profile);
        return volunteerFacade.buildProfileResponse(profile);
    }

    /**
     * 修改志愿者档案状态
     */
    @Transactional
    public VolunteerProfileResponse updateProfileStatus(Long profileId, String status) {
        requireWorker();
        VolunteerProfile profile = volunteerProfileMapper.requireById(profileId);
        profile.setStatus(VolunteerProfileStatus.get(status));
        profile.setUpdateTime(new Date());
        volunteerProfileMapper.updateById(profile);
        return volunteerFacade.buildProfileResponse(profile);
    }

    /**
     * 创建排班
     */
    @Transactional
    public VolunteerShiftResponse addShift(VolunteerShiftAddRequest request) {
        User login = requireWorker();
        checkVolunteerActive(request.getVolunteerId());

        VolunteerShift shift = request.create(login.getId());
        checkTimeConflict(shift, null);
        volunteerShiftMapper.insert(shift);
        eventPublisher.publishEvent(new VolunteerShiftAddEvent(shift, login));
        return volunteerFacade.buildShiftResponse(shift);
    }

    /**
     * 获取排班详情
     */
    public VolunteerShiftResponse getShift(Long shiftId) {
        VolunteerShift shift = volunteerShiftMapper.requireById(shiftId);
        User login = requireLoginUser();
        requirePermission(login.isWorker() || login.is(shift.getVolunteerId()));
        return volunteerFacade.buildShiftResponse(shift);
    }

    /**
     * 查询排班
     */
    public Page<VolunteerShiftResponse> getShifts(VolunteerShiftQueryParams query, PageParams page) {
        User login = requireLoginUser();
        if (!login.isWorker()) // 非管理员只能查看自己的排版
            query.setVolunteer(login.getId());

        Page<VolunteerShift> result = volunteerShiftMapper.queryByRequest(query).page(page);
        return volunteerFacade.buildShiftPage(result);
    }

    /**
     * 修改排班
     */
    @Transactional
    public VolunteerShiftResponse updateShift(Long shiftId, VolunteerShiftUpdateRequest request) {
        User login = requireWorker();
        VolunteerShift shift = volunteerShiftMapper.requireById(shiftId);
        request.applyTo(shift);
        checkVolunteerActive(shift.getVolunteerId());
        checkTimeConflict(shift, shiftId);
        volunteerShiftMapper.updateById(shift);
        eventPublisher.publishEvent(new VolunteerShiftAddEvent(shift, login));
        return volunteerFacade.buildShiftResponse(shift);
    }

    /**
     * 修改排班状态
     */
    @Transactional
    public VolunteerShiftResponse updateShiftStatus(Long shiftId, VolunteerShiftStatusUpdateRequest request) {
        User login = requireLoginUser();
        VolunteerShift shift = volunteerShiftMapper.requireById(shiftId);
        VolunteerShiftStatus status = VolunteerShiftStatus.get(request.getStatus());
        if (status.requireWorker())
            requireWorker();
        if (status.requireSelf())
            requirePermission(login.is(shift.getVolunteerId()));
        require(status.canSwitchFrom(shift.getStatus()), "状态错误");

        VolunteerShiftStatusRecord record = request.create(shift);
        request.applyTo(shift);
        volunteerShiftMapper.updateById(shift);
        volunteerShiftStatusRecordMapper.insert(record);
        eventPublisher.publishEvent(new VolunteerShiftStatusEvent(shift, record));
        return volunteerFacade.buildShiftResponse(shift);
    }

    /**
     * 提交服务记录
     */
    @Transactional
    public VolunteerServiceRecordResponse addServiceRecord(Long shiftId, VolunteerServiceRecordAddRequest request) {
        VolunteerShift shift = volunteerShiftMapper.requireById(shiftId);
        User login = requireLoginUser();
        requirePermission(login.isWorker() || login.is(shift.getVolunteerId()));
        requireEqual(VolunteerShiftStatus.COMPLETED, shift.getStatus(), "服务任务未完成记录");
        require(!volunteerServiceRecordMapper.queryByShift(shiftId).exists(), "该排班已存在服务记录");

        VolunteerServiceRecord record = request.create(shiftId, shift.getVolunteerId());
        volunteerServiceRecordMapper.insert(record);
        eventPublisher.publishEvent(new VolunteerRecordAddEvent(shift, record, login));

        return volunteerFacade.buildServiceRecordResponse(record);
    }

    /**
     * 获取服务记录详情
     */
    public VolunteerServiceRecordResponse getServiceRecord(Long recordId) {
        VolunteerServiceRecord record = volunteerServiceRecordMapper.requireById(recordId);
        User login = requireLoginUser();
        requirePermission(login.isWorker() || login.is(record.getVolunteerId()));
        return volunteerFacade.buildServiceRecordResponse(record);
    }

    /**
     * 查询服务记录
     */
    public Page<VolunteerServiceRecordResponse> getServiceRecords(VolunteerServiceRecordQueryParams query, PageParams page) {
        User login = requireLoginUser();
        if (!login.isWorker())
            query.setVolunteer(login.getId());

        Page<VolunteerServiceRecord> result = volunteerServiceRecordMapper.queryByRequest(query).page(page);
        return volunteerFacade.buildServiceRecordPage(result);
    }

    /**
     * 审核服务记录
     */
    @Transactional
    public VolunteerServiceRecordResponse updateServiceRecordStatus(Long recordId, VolunteerRecordReviewRequest request) {
        User login = requireLoginUser();
        VolunteerServiceRecord record = volunteerServiceRecordMapper.requireById(recordId);
        VolunteerRecordStatus status = VolunteerRecordStatus.get(request.getStatus());
        VolunteerRecordStatus oldStatus = record.getStatus();
        require(status.canChangeFrom(oldStatus), "当前服务记录不可审核通过");
        if (status.isReview()) {
            requirePermission(login.isWorker());
        } else {
            requirePermission(login.is(record.getVolunteerId()));
        }

        request.applyTo(record, login);
        volunteerServiceRecordMapper.updateById(record);
        eventPublisher.publishEvent(new VolunteerRecordStatusEvent(record, login, oldStatus));
        return volunteerFacade.buildServiceRecordResponse(record);
    }

    /**
     * 创建激励记录
     */
    @Transactional
    public VolunteerRewardResponse addReward(VolunteerRewardAddRequest request) {
        User login = requireWorker();
        requireVolunteer(request.getVolunteerId());
        VolunteerReward reward = request.create(login.getId());
        volunteerRewardMapper.insert(reward);
        return volunteerFacade.buildRewardResponse(reward);
    }

    /**
     * 获取激励详情
     */
    public VolunteerRewardResponse getReward(Long rewardId) {
        VolunteerReward reward = volunteerRewardMapper.requireById(rewardId);
        User login = requireLoginUser();
        requirePermission(login.isWorker() || login.is(reward.getVolunteerId()));
        return volunteerFacade.buildRewardResponse(reward);
    }

    /**
     * 查询激励记录
     */
    public Page<VolunteerRewardResponse> getRewards(VolunteerRewardQueryParams query, PageParams page) {
        User login = requireLoginUser();
        if (!login.isWorker())
            query.setVolunteer(login.getId());

        Page<VolunteerReward> result = volunteerRewardMapper.queryByRequest(query).page(page);
        return volunteerFacade.buildRewardPage(result);
    }

    /**
     * 发放激励
     */
    @Transactional
    public VolunteerRewardResponse issueReward(Long rewardId) {
        User login = requireWorker();
        VolunteerReward reward = volunteerRewardMapper.requireById(rewardId);
        requireEqual(VolunteerRewardStatus.PENDING, reward.getStatus(), "当前激励不可发放");
        reward.setStatus(VolunteerRewardStatus.ISSUED);
        reward.setIssuerId(login.getId());
        reward.setIssueTime(new Date());
        reward.setUpdateTime(new Date());
        volunteerRewardMapper.updateById(reward);
        return volunteerFacade.buildRewardResponse(reward);
    }

    /**
     * 获取当前工作人员
     */
    private User requireWorker() {
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        return login;
    }

    /**
     * 校验招募计划当前可报名
     */
    private void assertRecruitmentOpen(VolunteerRecruitment recruitment) {
        requireEqual(VolunteerRecruitmentStatus.PUBLISHED, recruitment.getStatus(), "当前招募计划未开放");
        Date now = new Date();
        if (recruitment.getStartTime() != null) {
            require(!now.before(recruitment.getStartTime()), "招募计划尚未开始");
        }
        if (recruitment.getEndTime() != null) {
            require(!now.after(recruitment.getEndTime()), "招募计划已结束");
        }
    }

    /**
     * 校验志愿者可参与排班
     */
    private void checkVolunteerActive(Long volunteerId) {
        requireVolunteer(volunteerId);
        VolunteerProfile profile = volunteerProfileMapper.queryByUserId(volunteerId).one(
                VolunteerProfile::getId, VolunteerProfile::getStatus);
        require(profile != null && profile.getStatus() == VolunteerProfileStatus.ACTIVE, "志愿者档案不可用");
    }

    /**
     * 校验用户是否具备志愿者身份
     */
    private void requireVolunteer(Long volunteerId) {
        User user = userService.requireById(volunteerId, User::getId, User::getRole);
        requirePermission(user.isVolunteer());
    }

    /**
     * 校验排班时间冲突
     */
    private void checkTimeConflict(VolunteerShift shift, Long excludeShiftId) {
        List<VolunteerShift> shifts = volunteerShiftMapper.queryByVolunteer(shift.getVolunteerId()).list();
        boolean conflict = shifts.stream()
                .filter(item -> excludeShiftId == null || !Objects.equals(item.getId(), excludeShiftId))
                .filter(item -> item.getStatus().isTimeEffective())
                .anyMatch(item -> isTimeConflict(item, shift));
        require(!conflict, "该志愿者在当前时段已有排班");
    }

    /**
     * 判断两个时间区间是否重叠
     */
    private boolean isTimeConflict(VolunteerShift existedShift, VolunteerShift shift) {
        Date start0 = existedShift.getStartTime();
        Date end0 = existedShift.getEndTime();
        Date start1 = shift.getStartTime();
        Date end1 = shift.getEndTime();
        if (start0 == null || end0 == null || start1 == null || end1 == null) {
            return false;
        }
        return start0.before(end1) && start1.before(end0);
    }

    @Autowired
    public void setServices(UserService userService) {
        this.userService = userService;
    }
}
