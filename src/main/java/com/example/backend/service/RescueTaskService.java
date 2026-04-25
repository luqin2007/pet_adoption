package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.event.RescueTaskAddEvent;
import com.example.backend.event.RescueTaskAssignEvent;
import com.example.backend.event.RescueTaskStatusEvent;
import com.example.backend.event.RescueTaskUpdateEvent;
import com.example.backend.mapper.LocationMapper;
import com.example.backend.mapper.RescueTaskAssignMapper;
import com.example.backend.mapper.RescueTaskMapper;
import com.example.backend.mapper.RescueTaskRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.backend.entity.property.ParentType.RESCUE_TASK;
import static com.example.backend.entity.property.RescueTaskAction.STATUS;
import static com.example.backend.entity.property.RescueTaskStatus.*;

/**
 * 救助任务管理
 */
@Service
@RequiredArgsConstructor
public class RescueTaskService extends BaseService<RescueTaskMapper, RescueTask> {

    private final LocationMapper locationMapper;
    private final RescueTaskAssignMapper rescueTaskAssignMapper;
    private final RescueTaskRecordMapper rescueTaskRecordMapper;
    private final InformationService informationService;

    private UserService userService;
    private FileService fileService;

    @Value("${key.rescue_task.uuid}")
    private String redisTemplate;
    @Value("${key.rescue_task.file}")
    private String fileTemplate;

    /**
     * 创建一个临时救助任务 id，有效期 30min
     */
    public String beginRescueTask() {
        // 检查用户
        requireLoginUser();
        return beginRedisUuid(redisTemplate, "");
    }

    /**
     * 正式创建救助任务
     */
    @Transactional
    public RescueTaskResponse addRescueTask(RescueTaskAddRequest request) {
        // 检查用户和任务
        User login = requireLoginUser();
        String uuid = request.getId();
        String redisKey = requireRedisUuid(redisTemplate, uuid);

        // 存储任务信息
        RescueTask task = request.createTask(login.getId());
        save(task);
        Location location = informationService.createValidatedLocation(request, RESCUE_TASK, task.getId(), login.getId());
        locationMapper.insert(location);
        RescueTaskRecord record = request.createRecord(task, login.getId());
        rescueTaskRecordMapper.insert(record);

        // 添加所有图片和视频
        fileService.saveTempMedias(fileTemplate, uuid, task, RESCUE_TASK);

        // 清理缓存
        redisHelper.deleteString(redisKey);
        eventPublisher.publishEvent(new RescueTaskAddEvent(task, location, login));
        return RescueTaskResponse.fromEntity(task);
    }

    /**
     * 获取救助任务信息
     */
    public RescueTaskResponse getRescueTask(Long taskId) {
        RescueTask task = requireById(taskId);
        User login = requireLoginUser();
        requirePermission(login.isWorker()
                || login.is(task.getUserId())
                || login.is(task.getApproveId()));

        return RescueTaskResponse.fromEntity(task);
    }

    /**
     * 上传媒体文件
     */
    public String uploadRescueTaskMedia(String uuid, MultipartFile file) {
        // 检查用户和任务
        requireLoginUser();
        requireRedisUuid(redisTemplate, uuid);
        // 上传文件
        TempFileInfo info = fileService.uploadTempMedia(file, null, uuid, fileTemplate, RESCUE_TASK);
        return info.getFilename();
    }

    /**
     * 更新救助任务信息
     */
    @Transactional
    public RescueTaskResponse updateRescueTask(Long taskId, RescueTaskUpdateRequest request) {
        // 校验
        RescueTask task = requireById(taskId);
        User login = requireLoginUser();
        requireEqual(CREATED, task.getStatus(), "exception.invalidate.rescue_task.update_after_approve");
        requirePermission(Objects.equals(task.getUserId(), login.getId()) || login.isWorker());

        // 更新任务信息
        request.applyTo(task);
        updateById(task);
        RescueTaskRecord record = request.createRecord(task, login.getId());
        rescueTaskRecordMapper.insert(record);

        // 更新位置信息
        Location location = locationMapper.queryByParent(RESCUE_TASK, taskId).one();
        if (location == null) { // 位置信息缺失
            location = informationService.createValidatedLocation(request, RESCUE_TASK, taskId, login.getId());
            locationMapper.insert(location);
        } else { // 更新
            informationService.applyValidatedLocation(request, location);
            locationMapper.updateById(location);
        }

        eventPublisher.publishEvent(new RescueTaskUpdateEvent(task, location, login));
        return RescueTaskResponse.fromEntity(task);
    }

    /**
     * 删除救助任务图片 (添加时)
     */
    public void deleteRescueTaskMedia(String uuid, String filename) {
        // 检查用户和任务
        requireLoginUser();
        requireRedisUuid(redisTemplate, uuid);

        // 删除媒体记录
        fileService.deleteTempFile(fileTemplate, uuid, filename, RESCUE_TASK);
    }

    /**
     * 删除救助任务图片
     */
    @Transactional
    public void deleteRescueTaskMedia(Long taskId, Long mediaId) {
        // 检查用户和任务状态
        User login = requireLoginUser();
        RescueTask task = requireById(taskId);
        requireEqual(CREATED, task.getStatus(), "exception.invalidate.rescue_task.update_after_approve");
        requirePermission(Objects.equals(task.getUserId(), login.getId()) || login.isWorker());

        // 删除媒体文件
        fileService.deleteMediaFile(mediaId, taskId, RESCUE_TASK);
    }

    /**
     * 删除救助任务
     */
    @Transactional
    public void deleteRescueTask(Long taskId) {
        // 检查用户和任务状态
        User login = requireLoginUser();
        RescueTask task = requireById(taskId);
        requireEqual(CREATED, task.getStatus(), "exception.invalidate.rescue_task.delete_after_approve");
        requirePermission(Objects.equals(task.getUserId(), login.getId()) || login.isWorker());

        // 删除媒体数据
        fileService.deleteAllMediaFiles(taskId, RESCUE_TASK);
        // 删除数据库
        removeById(taskId);
    }

    /**
     * 更新救助任务状态
     */
    @Transactional
    public void updateRescueTaskStatus(Long taskId, RescueTaskRecordStatusUpdateRequest request) {
        // 任务校验
        RescueTask task = requireById(taskId);
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        RescueTaskRecord record = request.createRescueTaskRecord(task, login.getId(), STATUS, task.getStatus());
        require(record.getStatusTo().canChangeFrom(task.getStatus()), "exception.invalidate.status");

        // 状态变更
        task.setStatus(record.getStatusTo());
        updateById(task);
        rescueTaskRecordMapper.insert(record);

        // 通知
        eventPublisher.publishEvent(new RescueTaskStatusEvent(task, record, login));
    }

    /**
     * 获取救助任务状态流转记录
     */
    public RescueTaskRecordsResponse getRescueTaskRecords(Long taskId) {
        List<RescueTaskRecord> result = rescueTaskRecordMapper.queryByTask(taskId).list();
        Map<Long, User> users = userService.groupById(
                result.stream().map(RescueTaskRecord::getUserId),
                User::getId, User::getUsername, User::getAvatar);
        return RescueTaskRecordsResponse.create(result, users);
    }

    /**
     * 获取救助任务信息列表
     */
    public Page<RescueTaskResponse> getRescueTasks(PageParams page) {
        Page<RescueTask> result = page(page.createPage());
        return convertDto(result, RescueTaskResponse::fromEntity);
    }

    /**
     * 分配救助任务
     */
    @Transactional
    public List<UserResponse> assignRescueTask(Long taskId, IdsRequest request) {
        // 校验权限
        User login = requireLoginUser();
        requirePermission(login.isWorker());
        RescueTask task = requireById(taskId);
        require(APPROVED == task.getStatus() || PROCESSING == task.getStatus(), "exception.invalidate.rescue_task.not_approved");

        // 任务分配
        Set<Long> addUsers = rescueTaskAssignMapper.queryUserByTask(taskId)
                .list(RescueTaskAssign::getUserId)
                .collect(Collectors.toSet()); // 已分配用户
        List<RescueTaskAssign> assigns = userService.listById(request.idSet(), User::getId).stream()
                .map(User::getId) // 过滤非法用户
                .filter(id -> !addUsers.contains(id)) // 过滤已分配用户
                // 创建记录
                .map(id -> RescueTaskAssign.create(taskId, id, login.getId()))
                .toList();
        rescueTaskAssignMapper.insert(assigns);

        eventPublisher.publishEvent(new RescueTaskAssignEvent(task, assigns, login));
        Set<Long> userIds = rescueTaskAssignMapper.queryUserByTask(taskId)
                .list(RescueTaskAssign::getUserId)
                .collect(Collectors.toSet()); // 所有已分配用户
        List<User> users = userService.listById(userIds,
                User::getId, User::getEmail);
        return users.stream().map(UserResponse::create).toList();
    }

    @Autowired
    public void setServices(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }
}
