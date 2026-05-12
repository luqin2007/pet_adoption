package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.*;
import com.example.backend.event.*;
import com.example.backend.facade.VolunteerFacade;
import com.example.backend.mapper.*;
import com.example.backend.util.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final VolunteerTaskMapper volunteerTaskMapper;
    private final LocationMapper locationMapper;

    private final VolunteerFacade volunteerFacade;
    private final InformationService informationService;

    private UserService userService;

    /**
     * 创建招募计划
     */
    @Transactional
    public VolunteerRecruitmentResponse addRecruitment(VolunteerRecruitmentAddRequest request) {
        User login = requireWorker();
        VolunteerRecruitment recruitment = request.create(login.getId());
        save(recruitment);
        saveLocation(request, ParentType.RECRUITMENT, recruitment.getId(), login.getId());
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
        requireEqual(VolunteerRecruitmentStatus.DRAFT, recruitment.getStatus(), "exception.invalidate.volunteer.recruitment.published_locked");
        request.applyTo(recruitment);
        updateById(recruitment);
        upsertLocation(request, ParentType.RECRUITMENT, recruitment.getId(), recruitment.getPublisherId());
        return volunteerFacade.buildRecruitmentResponse(recruitment);
    }

    /**
     * 修改招募计划状态
     */
    @Transactional
    public VolunteerRecruitmentResponse updateRecruitmentStatus(Long recruitmentId, VolunteerRecruitmentStatusUpdateRequest request) {
        requireWorker();
        VolunteerRecruitment recruitment = requireById(recruitmentId);
        VolunteerRecruitmentStatus status = VolunteerRecruitmentStatus.get(request.getStatus());
        require(status.canChangeFrom(recruitment.getStatus()), "exception.invalidate.status");

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
        if (login.isVolunteer())
            throw ServiceException.conflict("exception.conflict.volunteer.already_volunteer");

        VolunteerRecruitment recruitment = requireById(request.getRecruitmentId());
        assertRecruitmentOpen(recruitment);
        if (volunteerApplicationMapper.queryByRecruitmentAndUser(recruitment.getId(), login.getId()).exists())
            throw ServiceException.conflict("exception.conflict.volunteer.application_duplicate");

        VolunteerApplication application = request.create(login.getId());
        volunteerApplicationMapper.insert(application);
        saveLocation(request, ParentType.VOLUNTEER_APP, application.getId(), login.getId());

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
        require(status.canSwitchFrom(application.getStatus()), "exception.invalidate.volunteer.application.status_invalid");

        // 申请状态
        Date now = new Date();
        application.setStatus(status, request.getReason(), login);
        application.setUpdateTime(now);
        volunteerApplicationMapper.updateById(application);

        if (status == VolunteerApplicationStatus.APPROVED) {
            // 更新用户角色
            User applicant = userService.requireById(application.getUserId());
            int role = applicant.getRole() == null ? 0 : applicant.getRole();
            applicant.setRole(role | UserRole.VOLUNTEER.getMask());
            userService.updateById(applicant);
            // 更新志愿者档案
            VolunteerProfile profile = volunteerProfileMapper.queryByUserId(application.getUserId()).one();
            if (profile == null) {
                profile = request.createProfile(application, applicant);
                volunteerProfileMapper.insert(profile);
            } else {
                request.applyTo(profile, application);
                volunteerProfileMapper.updateById(profile);
            }
            upsertProfileLocation(application, profile);
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
        upsertLocation(request, ParentType.VOLUNTEER_PROFILE, profile.getId(), profile.getUserId());
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
        VolunteerTask task;
        if (request.getTaskId() == null) { // 新任务
            task = request.createTask();
            volunteerTaskMapper.insert(task);
            Location location = informationService.createValidatedLocation(request, ParentType.VOLUNTEER_TASK, task.getId(), login.getId());
            locationMapper.insert(location);
            task.setLocationId(location.getId());
            volunteerTaskMapper.updateById(task);
        } else {
            task = volunteerTaskMapper.requireById(request.getTaskId());
        }

        checkTimeConflict(shift, null);
        shift.setTaskId(task.getId());
        volunteerShiftMapper.insert(shift);
        eventPublisher.publishEvent(new VolunteerShiftAddEvent(shift, task, login));
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

        Set<Long> taskRecordIds = queryTaskRecordIds(query);
        if (taskRecordIds != null && taskRecordIds.isEmpty()) {
            Page<VolunteerShift> result = page.createPage();
            result.setRecords(List.of());
            result.setTotal(0);
            return volunteerFacade.buildShiftPage(result);
        }

        Page<VolunteerShift> result = volunteerShiftMapper.queryByRequest(query, taskRecordIds).page(page);
        return volunteerFacade.buildShiftPage(result);
    }

    /**
     * 修改排班
     */
    @Transactional
    public VolunteerShiftResponse updateShift(Long shiftId, VolunteerShiftUpdateRequest request) {
        User login = requireWorker();
        VolunteerShift shift = volunteerShiftMapper.requireById(shiftId);
        require(!Set.of(VolunteerShiftStatus.IN_PROGRESS, VolunteerShiftStatus.COMPLETED).contains(shift.getStatus()),
                "exception.invalidate.volunteer.shift.status_invalid");
        request.applyTo(shift);
        checkVolunteerActive(shift.getVolunteerId());
        checkTimeConflict(shift, shiftId);
        volunteerShiftMapper.updateById(shift);

        Long taskId = shift.getTaskId();
        VolunteerTask task = volunteerTaskMapper.requireById(taskId);
        eventPublisher.publishEvent(new VolunteerShiftUpdateEvent(shift, task, login));
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
        boolean selfRejectAssigned = status == VolunteerShiftStatus.CANCELLED
                && shift.getStatus() == VolunteerShiftStatus.ASSIGNED
                && login.is(shift.getVolunteerId());
        if (status.requireWorker() && !selfRejectAssigned)
            requireWorker();
        if (status.requireSelf())
            requirePermission(login.is(shift.getVolunteerId()));
        require(status.canSwitchFrom(shift.getStatus()), "exception.invalidate.volunteer.shift.status_invalid");

        VolunteerShiftStatusRecord record = request.create(shift);
        request.applyTo(shift);
        volunteerShiftMapper.updateById(shift);
        volunteerShiftStatusRecordMapper.insert(record);
        VolunteerTask task = volunteerTaskMapper.requireById(shift.getTaskId());
        eventPublisher.publishEvent(new VolunteerShiftStatusEvent(shift, task, record, login));
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
        requireEqual(VolunteerShiftStatus.COMPLETED, shift.getStatus(), "exception.invalidate.volunteer.record.shift_not_completed");
        if (volunteerServiceRecordMapper.queryByShift(shiftId).exists())
            throw ServiceException.conflict("exception.conflict.volunteer.record.exists");

        VolunteerServiceRecord record = request.create(shiftId, shift.getVolunteerId());
        volunteerServiceRecordMapper.insert(record);
        VolunteerTask task = volunteerTaskMapper.requireById(shift.getTaskId());
        eventPublisher.publishEvent(new VolunteerRecordAddEvent(shift, task, record, login));

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
        require(status.canChangeFrom(oldStatus), "exception.invalidate.volunteer.record.review_status_invalid");
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
        requireEqual(VolunteerRewardStatus.PENDING, reward.getStatus(), "exception.invalidate.volunteer.reward.issue_status_invalid");
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
        requireEqual(VolunteerRecruitmentStatus.PUBLISHED, recruitment.getStatus(), "exception.invalidate.volunteer.recruitment.not_open");
        Date now = new Date();
        if (recruitment.getStartTime() != null) {
            require(!now.before(recruitment.getStartTime()), "exception.invalidate.volunteer.recruitment.not_started");
        }
        if (recruitment.getEndTime() != null) {
            require(!now.after(recruitment.getEndTime()), "exception.invalidate.volunteer.recruitment.ended");
        }
    }

    /**
     * 校验志愿者可参与排班
     */
    private void checkVolunteerActive(Long volunteerId) {
        requireVolunteer(volunteerId);
        VolunteerProfile profile = volunteerProfileMapper.queryByUserId(volunteerId).one(
                VolunteerProfile::getId, VolunteerProfile::getStatus);
        require(profile != null && profile.getStatus() == VolunteerProfileStatus.ACTIVE, "exception.invalidate.volunteer.profile.unavailable");
    }

    /**
     * 校验用户是否具备志愿者身份
     */
    private void requireVolunteer(Long volunteerId) {
        User user = userService.requireById(volunteerId, User::getId, User::getRole);
        requirePermission(user.isVolunteer());
    }

    private void saveLocation(LocationRequest request, ParentType parentType, Long parentId, Long userId) {
        Location location = informationService.createValidatedLocation(request, parentType, parentId, userId);
        locationMapper.insert(location);
    }

    private void upsertLocation(LocationRequest request, ParentType parentType, Long parentId, Long userId) {
        Location location = locationMapper.queryByParent(parentType, parentId).one();
        if (location == null) {
            saveLocation(request, parentType, parentId, userId);
            return;
        }
        informationService.applyValidatedLocation(request, location);
        location.setUserId(userId);
        locationMapper.updateById(location);
    }

    private void upsertProfileLocation(VolunteerApplication application, VolunteerProfile profile) {
        Location applicationLocation = locationMapper
                .queryByParent(ParentType.VOLUNTEER_APP, application.getId())
                .require();
        Location profileLocation = locationMapper
                .queryByParent(ParentType.VOLUNTEER_PROFILE, profile.getId())
                .one();
        if (profileLocation == null) {
            profileLocation = new Location(null,
                    profile.getId(),
                    ParentType.VOLUNTEER_PROFILE,
                    profile.getUserId(),
                    applicationLocation.getProvince(),
                    applicationLocation.getCity(),
                    applicationLocation.getDistrict(),
                    applicationLocation.getDetailAddress(),
                    new Date());
            locationMapper.insert(profileLocation);
            return;
        }
        profileLocation.setUserId(profile.getUserId());
        profileLocation.setProvince(applicationLocation.getProvince());
        profileLocation.setCity(applicationLocation.getCity());
        profileLocation.setDistrict(applicationLocation.getDistrict());
        profileLocation.setDetailAddress(applicationLocation.getDetailAddress());
        profileLocation.setCreateTime(new Date());
        locationMapper.updateById(profileLocation);
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
        if (conflict)
            throw ServiceException.conflict("exception.conflict.volunteer.shift.time_conflict");
    }

    private Set<Long> queryTaskRecordIds(VolunteerShiftQueryParams query) {
        boolean hasKeyword = query.getKeyword() != null && !query.getKeyword().isBlank();
        boolean hasTaskFilter = hasKeyword
                || query.getTaskType() != null
                || query.getTime0() != null
                || query.getTime1() != null;
        if (!hasTaskFilter) return null;

        return volunteerTaskMapper.lambdaQuery()
                .in(VolunteerTask::getTaskType, VolunteerTaskType::get, query.getTaskType())
                .in(VolunteerTask::getStartTime, query.getTime0(), query.getTime1())
                .like(VolunteerTask::getTitle, query.getKeyword())
                .list(VolunteerTask::getId)
                .collect(Collectors.toSet());
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
