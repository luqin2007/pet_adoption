package com.example.backend.facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.ParentType;
import com.example.backend.mapper.*;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 志愿者模块聚合查询
 */
@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class VolunteerFacade {

    private final VolunteerRecruitmentMapper volunteerRecruitmentMapper;
    private final VolunteerShiftMapper volunteerShiftMapper;
    private final VolunteerShiftStatusRecordMapper volunteerShiftStatusRecordMapper;
    private final VolunteerServiceRecordMapper volunteerServiceRecordMapper;
    private final VolunteerTaskMapper volunteerTaskMapper;
    private final UserService userService;
    private final LocationMapper locationMapper;

    /**
     * 组装招募计划响应
     */
    public VolunteerRecruitmentResponse buildRecruitmentResponse(VolunteerRecruitment recruitment) {
        User publisher = userService.selectById(recruitment.getPublisherId(),
                User::getId, User::getUsername, User::getAvatar);
        Location location = locationMapper
                .queryByParent(ParentType.RECRUITMENT, recruitment.getId())
                .require();
        return VolunteerRecruitmentResponse.create(recruitment, location, publisher);
    }

    /**
     * 组装招募计划分页响应
     */
    public Page<VolunteerRecruitmentResponse> buildRecruitmentPage(Page<VolunteerRecruitment> result) {
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().map(VolunteerRecruitment::getPublisherId),
                User::getId, User::getUsername, User::getAvatar);
        Set<Long> recruitmentIds = result.getRecords().stream()
                .map(VolunteerRecruitment::getId)
                .collect(Collectors.toSet());
        Map<Long, Location> locations = locationMapper
                .queryByParents(ParentType.RECRUITMENT, recruitmentIds)
                .group(Location::getParentId);
        return convert(result, recruitment ->
                VolunteerRecruitmentResponse.createBatch(recruitment, locations, users));
    }

    /**
     * 组装志愿者申请响应
     */
    public VolunteerApplicationResponse buildApplicationResponse(VolunteerApplication application) {
        VolunteerRecruitment recruitment = volunteerRecruitmentMapper.selectById(application.getRecruitmentId(),
                VolunteerRecruitment::getId, VolunteerRecruitment::getTitle);
        Map<Long, User> users = userService.groupById(
                Stream.of(application.getUserId(), application.getReviewerId()),
                User::getId, User::getUsername, User::getAvatar);
        Location location = locationMapper
                .queryByParent(ParentType.VOLUNTEER_APP, application.getId())
                .require();
        return VolunteerApplicationResponse.create(application,
                recruitment, location,
                users.get(application.getUserId()),
                users.get(application.getReviewerId()));
    }

    /**
     * 组装志愿者申请分页响应
     */
    public Page<VolunteerApplicationResponse> buildApplicationPage(Page<VolunteerApplication> result) {
        Set<Long> recruitmentIds = result.getRecords().stream()
                .map(VolunteerApplication::getRecruitmentId)
                .collect(Collectors.toSet());
        Map<Long, VolunteerRecruitment> recruitments = volunteerRecruitmentMapper.groupById(recruitmentIds,
                VolunteerRecruitment::getId, VolunteerRecruitment::getTitle);
        Set<Long> applicationIds = result.getRecords().stream()
                .map(VolunteerApplication::getId)
                .collect(Collectors.toSet());
        Map<Long, Location> locations = locationMapper
                .queryByParents(ParentType.VOLUNTEER_APP, applicationIds)
                .group(Location::getParentId);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(item -> Stream.of(item.getUserId(), item.getReviewerId())),
                User::getId, User::getUsername, User::getAvatar);
        return convert(result, application ->
                VolunteerApplicationResponse.createBatch(application, recruitments, locations, users));
    }

    /**
     * 组装志愿者档案响应
     */
    public VolunteerProfileResponse buildProfileResponse(VolunteerProfile profile) {
        User user = userService.selectById(profile.getUserId(),
                User::getId, User::getUsername, User::getAvatar);
        Location location = locationMapper
                .queryByParent(ParentType.VOLUNTEER_PROFILE, profile.getId())
                .require();
        return VolunteerProfileResponse.create(profile, location, user);
    }

    /**
     * 组装志愿者档案分页响应
     */
    public Page<VolunteerProfileResponse> buildProfilePage(Page<VolunteerProfile> result) {
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().map(VolunteerProfile::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        Set<Long> profileIds = result.getRecords().stream()
                .map(VolunteerProfile::getId)
                .collect(Collectors.toSet());
        Map<Long, Location> locations = locationMapper
                .queryByParents(ParentType.VOLUNTEER_PROFILE, profileIds)
                .group(Location::getParentId);
        return convert(result, profile -> VolunteerProfileResponse.createBatch(profile, locations, users));
    }

    /**
     * 组装排班响应
     */
    public VolunteerShiftResponse buildShiftResponse(VolunteerShift shift) {
        Map<Long, User> users = userService.groupById(
                Stream.of(shift.getVolunteerId(), shift.getAssignerId()),
                User::getId, User::getUsername, User::getAvatar);
        VolunteerServiceRecord record = volunteerServiceRecordMapper.queryByShift(shift.getId()).one(
                VolunteerServiceRecord::getId, VolunteerServiceRecord::getShiftId, VolunteerServiceRecord::getStatus);
        List<VolunteerShiftStatusRecord> statusRecords = volunteerShiftStatusRecordMapper.queryByShift(shift.getId()).list();
        VolunteerTask task = volunteerTaskMapper.requireById(shift.getTaskId());
        Location location = getLocation(task);
        return VolunteerShiftResponse.create(shift, task, location,
                users.get(shift.getVolunteerId()),
                users.get(shift.getAssignerId()),
                record, statusRecords);
    }

    /**
     * 组装排班分页响应
     */
    public Page<VolunteerShiftResponse> buildShiftPage(Page<VolunteerShift> result) {
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(item -> Stream.of(item.getVolunteerId(), item.getAssignerId())),
                User::getId, User::getUsername, User::getAvatar);
        Set<Long> shiftIds = result.getRecords().stream()
                .map(VolunteerShift::getId)
                .collect(Collectors.toSet());
        Set<Long> taskIds = result.getRecords().stream()
                .map(VolunteerShift::getTaskId)
                .collect(Collectors.toSet());
        Map<Long, VolunteerTask> tasks = volunteerTaskMapper.groupById(taskIds);
        Map<Long, Location> locations = tasks.values().stream()
                .collect(Collectors.toMap(VolunteerTask::getId, this::getLocation));
        Map<Long, VolunteerServiceRecord> records = volunteerServiceRecordMapper.queryByShifts(shiftIds).group(
                VolunteerServiceRecord::getShiftId,
                VolunteerServiceRecord::getId, VolunteerServiceRecord::getShiftId, VolunteerServiceRecord::getStatus);
        Map<Long, List<VolunteerShiftStatusRecord>> statusRecords = volunteerShiftStatusRecordMapper.queryByShifts(shiftIds)
                .groupList(VolunteerShiftStatusRecord::getShiftId);
        return convert(result, shift -> VolunteerShiftResponse.createBatch(shift, tasks, locations, users, records, statusRecords));
    }

    /**
     * 组装服务记录响应
     */
    public VolunteerServiceRecordResponse buildServiceRecordResponse(VolunteerServiceRecord record) {
        VolunteerShift shift = volunteerShiftMapper.selectById(record.getShiftId(),
                VolunteerShift::getId, VolunteerShift::getTaskId);
        VolunteerTask task = volunteerTaskMapper.requireById(shift.getTaskId());
        Map<Long, User> users = userService.groupById(
                Stream.of(record.getVolunteerId(), record.getReviewerId()),
                User::getId, User::getUsername, User::getAvatar);
        return VolunteerServiceRecordResponse.create(record,
                task,
                users.get(record.getVolunteerId()),
                users.get(record.getReviewerId()));
    }

    /**
     * 组装服务记录分页响应
     */
    public Page<VolunteerServiceRecordResponse> buildServiceRecordPage(Page<VolunteerServiceRecord> result) {
        Set<Long> shiftIds = result.getRecords().stream()
                .map(VolunteerServiceRecord::getShiftId)
                .collect(Collectors.toSet());
        Map<Long, VolunteerShift> shifts = volunteerShiftMapper.groupById(shiftIds, VolunteerShift::getTaskId);
        Set<Long> taskIds = shifts.values().stream()
                .map(VolunteerShift::getTaskId)
                .collect(Collectors.toSet());
        Map<Long, VolunteerTask> tasks = volunteerTaskMapper.groupById(taskIds);
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(item -> Stream.of(item.getVolunteerId(), item.getReviewerId())),
                User::getId, User::getUsername, User::getAvatar);
        return convert(result, record -> VolunteerServiceRecordResponse.createBatch(record, tasks, users));
    }

    /**
     * 组装激励响应
     */
    public VolunteerRewardResponse buildRewardResponse(VolunteerReward reward) {
        Map<Long, User> users = userService.groupById(
                Stream.of(reward.getVolunteerId(), reward.getIssuerId()),
                User::getId, User::getUsername, User::getAvatar);
        return VolunteerRewardResponse.create(reward, users.get(reward.getVolunteerId()), users.get(reward.getIssuerId()));
    }

    /**
     * 组装激励分页响应
     */
    public Page<VolunteerRewardResponse> buildRewardPage(Page<VolunteerReward> result) {
        Map<Long, User> users = userService.groupById(
                result.getRecords().stream().flatMap(item -> Stream.of(item.getVolunteerId(), item.getIssuerId())),
                User::getId, User::getUsername, User::getAvatar);
        return convert(result, reward -> VolunteerRewardResponse.createBatch(reward, users));
    }

    /**
     * 通用分页转换
     */
    private <T, R> Page<R> convert(Page<T> source, Function<T, R> mapper) {
        Page<R> page = PageDTO.of(source.getCurrent(), source.getSize(), source.getTotal());
        page.setRecords(source.getRecords().stream().map(mapper).toList());
        return page;
    }

    private Location getLocation(VolunteerTask task) {
        if (task.getLocationId() != null) {
            Location location = locationMapper.selectById(task.getLocationId());
            if (location != null) return location;
        }
        return locationMapper.queryByParent(ParentType.VOLUNTEER_TASK, task.getId()).one();
    }
}
