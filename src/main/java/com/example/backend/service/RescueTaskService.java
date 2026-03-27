package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.entity.property.MediaType;
import com.example.backend.entity.property.RescueTaskStatus;
import com.example.backend.mapper.*;
import com.example.backend.util.FileUtils;
import com.example.backend.util.NotificationEvent;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.backend.entity.property.MediaType.IMAGE;
import static com.example.backend.entity.property.ParentType.RESCUE_TASK;
import static com.example.backend.entity.property.RescueTaskAction.*;
import static com.example.backend.entity.property.RescueTaskStatus.CREATED;
import static com.example.backend.entity.property.UserRole.WORKER;
import static com.example.backend.util.C.KEY_RESCUE_TASK;
import static com.example.backend.util.C.KEY_RESCUE_TASK_MEDIA;

@Service
@RequiredArgsConstructor
public class RescueTaskService extends BaseService<RescueTaskMapper, RescueTask> {

    private final RescueTaskLocationMapper rescueTaskLocationMapper;
    private final RescueTaskAssignMapper rescueTaskAssignMapper;
    private final RescueTaskRecordMapper rescueTaskRecordMapper;
    private final MediaFileMapper mediaFileMapper;

    private UserService userService;

    private static final long KEY_TIMEOUT_MINUTES = 30;

    @Value("${host.address}")
    private String host;

    /**
     * 创建一个临时救助任务 id，有效期 30min
     */
    public String beginRescueTask() {
        // 检查用户
        getLoginUser();

        // 生成临时 task id
        String uuid = StringUtils.randomUUID(KEY_RESCUE_TASK, redisHelper, 10);
        String redisKey = String.format(KEY_RESCUE_TASK, uuid);

        // 30min 填写时间
        redisHelper.putString(redisKey, "", KEY_TIMEOUT_MINUTES);
        return uuid;
    }

    /**
     * 正式创建救助任务
     */
    @Transactional
    public RescueTaskResponse addRescueTask(RescueTaskAddRequest request) {
        // 检查用户和任务
        User login = getLoginUser();
        String uuid = request.getId();
        String redisKey = String.format(KEY_RESCUE_TASK, uuid);
        redisHelper.putString(redisKey, "添加超时，请刷新重试");

        // 存储任务信息
        RescueTask task = request.createTask(login.getId());
        save(task);
        rescueTaskLocationMapper.insert(request.createLocation(task.getId(), login.getId()));
        RescueTaskRecord record =
                RescueTaskRecord.create(task, login.getId(), CREATE, CREATED, task.getSummary());
        rescueTaskRecordMapper.insert(record);

        // 添加所有图片和视频
        String mediaKey = String.format(KEY_RESCUE_TASK_MEDIA, uuid);
        List<MediaFile> infos = redisHelper.getObjectsFromHash(mediaKey, TempFileInfo.class)
                // 转移临时文件
                .filter(data -> FileUtils.transferTempFile(data, uuid, task.getId(), RESCUE_TASK))
                // 更新媒体信息
                .sorted()
                .map(media -> media.createMediaFile(task.getId(), RESCUE_TASK))
                .toList();
        mediaFileMapper.insert(infos);

        // 清理缓存
        redisHelper.deleteString(redisKey);
        redisHelper.deleteObject(mediaKey);
        FileUtils.tryDeleteDirectory(FileUtils.generateTempPath(RESCUE_TASK, uuid), false);

        // 通知
        String notificationTitle = "新任务";
        String notificationContent = task.getSummary();
        eventPublisher.publishEvent(NotificationEvent.system(notificationTitle, notificationContent,
                "/tasks/" + task.getId(), WORKER));
        String emailTitle = "新任务：" + task.getSummary();
        String emailContent = buildALabel(task.getSummary(), task.getId()) + "<div>" + task.getDescription() + "</div>";
        eventPublisher.publishEvent(NotificationEvent.mailToRoles(emailTitle, emailContent, WORKER));
        eventPublisher.publishEvent(NotificationEvent.system(emailTitle, emailContent, buildTaskUrl(task.getId()), WORKER));
        return RescueTaskResponse.fromEntity(task);
    }

    /**
     * 获取救助任务信息
     */
    public RescueTaskResponse getRescueTask(Long taskId) {
        RescueTask task = requireById(taskId);
        return RescueTaskResponse.fromEntity(task);
    }

    /**
     * 上传媒体文件
     */
    public String uploadRescueTaskMedia(String uuid, MultipartFile file) {
        // 检查用户和任务
        User user = getLoginUser();
        String redisKey = String.format(KEY_RESCUE_TASK, uuid);
        redisHelper.requireString(redisKey, "添加超时，请刷新重试");
        redisHelper.expireString(redisKey, KEY_TIMEOUT_MINUTES);

        // 上传文件
        Pair<String, MediaType> extAndType = FileUtils.getFileExtensionAndType(file);
        Date now = new Date();
        String name = FileUtils.getNameWithoutExtension(file.getOriginalFilename());
        String filename = FileUtils.generateFilename(name, now, extAndType.getFirst());
        Path targetPath = FileUtils.generateTempPath(RESCUE_TASK, uuid);
        FileUtils.upload(file, filename, targetPath);

        // 存储媒体数据
        TempFileInfo info = new TempFileInfo(
                filename,
                name,
                user.getId(),
                extAndType.getSecond(),
                now);
        String hashKey = String.format(KEY_RESCUE_TASK_MEDIA, uuid);
        redisHelper.putObjectToHash(hashKey, filename, info);
        redisHelper.expireObject(hashKey, KEY_TIMEOUT_MINUTES);
        return filename;
    }

    /**
     * 更新救助任务信息
     */
    @Transactional
    public RescueTaskResponse updateRescueTask(Long taskId, RescueTaskUpdateRequest request) {
        // 校验
        RescueTask task = requireById(taskId);
        User login = getLoginUser();
        if (!Objects.equals(task.getStatus(), CREATED)) {
            // 刚创建，允许创建者和工作人员修改
            requirePermission(Objects.equals(task.getUserId(), login.getId()) || login.isWorker());
        } else {
            throw ServiceException.invalidate("无法修改已通过的任务");
        }

        // 更新任务信息
        request.applyTo(task);
        updateById(task);
        RescueTaskRecord record = RescueTaskRecord.create(task, login.getId(), UPDATE, task.getStatus(), request.getReason());
        rescueTaskRecordMapper.insert(record);

        // 更新位置信息
        Location location = rescueTaskLocationMapper.selectOne(rescueTaskLocationMapper.queryByTask(taskId));
        if (location == null) { // 位置信息缺失
            rescueTaskLocationMapper.insert(request.createLocation(taskId, login.getId()));
        } else { // 更新
            request.applyTo(location);
            rescueTaskLocationMapper.updateById(location);
        }

        // 通知
        String title = "救援任务已更新";
        String content = task.getSummary();
        eventPublisher.publishEvent(NotificationEvent.system(title, content, buildTaskUrl(task.getId()), WORKER));
        return RescueTaskResponse.fromEntity(task);
    }

    /**
     * 删除救助任务图片 (添加时)
     */
    public void deleteRescueTaskMedia(String uuid, String filename) {
        // 检查用户和任务
        getLoginUser();
        String redisKey = String.format(KEY_RESCUE_TASK, uuid);
        redisHelper.requireString(redisKey, "添加超时，请刷新重试");
        redisHelper.expireString(redisKey, KEY_TIMEOUT_MINUTES);

        // 删除媒体记录
        String listKey = String.format(KEY_RESCUE_TASK_MEDIA, uuid);
        redisHelper.deleteObjectFromHash(listKey, filename);
        Path file = FileUtils.generateTempPath(RESCUE_TASK, uuid, filename);
        FileUtils.tryDeleteFile(file);
    }

    /**
     * 删除救助任务图片
     */
    @Transactional
    public void deleteRescueTaskMedia(Long taskId, Long mediaId) {
        // 检查用户和任务状态
        User login = getLoginUser();
        MediaFile media = mediaFileMapper.requireById(mediaId);
        requireEqual(RESCUE_TASK, media.getParentType(), "图片/视频不匹配");
        requireEqual(taskId, media.getParentId(), "图片/视频不匹配");
        RescueTask task = requireById(media.getParentId());
        requireEqual(CREATED, task.getStatus(), "无法修改已通过的任务");
        requirePermission(Objects.equals(task.getUserId(), login.getId()) || login.isWorker());

        // 删除媒体文件
        mediaFileMapper.deleteById(mediaId);
        Path file = FileUtils.generateFilePath(RESCUE_TASK, media.getParentId(), media.getFilename());
        FileUtils.tryDeleteFile(file);
        FileUtils.tryDeleteDirectory(file.getParent(), true);

        // 图片：重置封面
        if (Objects.equals(IMAGE, media.getType()) && media.getIsCover()) {
            MediaFile latestImage = mediaFileMapper.selectOne(mediaFileMapper.queryLatestImageId(RESCUE_TASK, task.getId()));
            if (latestImage != null)
                mediaFileMapper.updateById(latestImage.getId(), MediaFile::getIsCover, true);
        }
    }

    /**
     * 删除救助任务
     */
    @Transactional
    public void deleteRescueTask(Long taskId) {
        // 检查用户和任务状态
        User login = getLoginUser();
        RescueTask task = requireById(taskId);
        requireEqual(CREATED, task.getStatus(), "无法删除已通过的任务");
        requirePermission(Objects.equals(task.getUserId(), login.getId()) || login.isWorker());

        // 删除数据库数据
        List<MediaFile> mediaFiles = mediaFileMapper.selectList(mediaFileMapper.queryIdAndFilename(RESCUE_TASK, taskId));
        mediaFileMapper.deleteByIds(mediaFiles);
        removeById(taskId);

        // 删除媒体文件
        mediaFiles.stream()
                .map(info -> FileUtils.generateFilePath(RESCUE_TASK, taskId, info.getFilename()))
                .forEach(FileUtils::tryDeleteFile);
        Path taskPath = FileUtils.generateFilePath(RESCUE_TASK, taskId);
        FileUtils.tryDeleteDirectory(taskPath, false);
    }

    /**
     * 更新救助任务状态
     */
    @Transactional
    public void updateRescueTaskStatus(Long taskId, RescueTaskRecordStatusUpdateRequest request) {
        // 任务校验
        RescueTask task = requireById(taskId);
        User login = getLoginUser();
        requirePermission(login.isWorker());

        // 状态变更
        RescueTaskRecord record = RescueTaskRecord.create(task, login.getId(), STATUS, task.getStatus(), request.getReason());
        task.setStatus(RescueTaskStatus.get(request.getStatus()));
        updateById(task);
        rescueTaskRecordMapper.insert(record);

        // 通知
        String title = "任务状态已更新";
        String content = task.getSummary() + ": " + request.getReason();
        eventPublisher.publishEvent(NotificationEvent.system(title, content, buildTaskUrl(task.getId()), WORKER));
    }

    /**
     * 获取救助任务状态流转记录
     */
    public RescueTaskRecordsResponse getRescueTaskRecords(Long taskId) {
        List<RescueTaskRecord> result = rescueTaskRecordMapper.selectList(rescueTaskRecordMapper.queryByTask(taskId));
        Set<Long> userIds = result.stream()
                .map(RescueTaskRecord::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> users = userService.groupById(userIds);
        return RescueTaskRecordsResponse.create(result, users);
    }

    /**
     * 获取救助任务信息列表
     */
    public Page<RescueTaskResponse> getRescueTasks(PageRequest page) {
        Page<RescueTask> result = page(page.createPage());
        return convertDto(result, RescueTaskResponse::fromEntity);
    }

    /**
     * 分配救助任务
     */
    @Transactional
    public List<UserResponse> assignRescueTask(Long taskId, IdsRequest request) {
        // 校验权限
        User login = getLoginUser();
        requirePermission(login.isWorker());
        RescueTask task = requireById(taskId);
        requireNotEqual(CREATED, task.getStatus(), "任务未通过审核");

        // 任务分配
        Set<Long> addUsers = rescueTaskAssignMapper.selectList(rescueTaskAssignMapper.queryUserIdByTask(taskId)).stream()
                .map(RescueTaskAssign::getUserId)
                .collect(Collectors.toSet()); // 已分配用户
        List<RescueTaskAssign> assigns = userService.listById(request.idSet(), User::getId).stream()
                .map(User::getId) // 过滤非法用户
                .filter(id -> !addUsers.contains(id)) // 过滤已分配用户
                // 创建记录
                .map(id -> RescueTaskAssign.create(taskId, id, login.getId()))
                .toList();
        rescueTaskAssignMapper.insert(assigns);

        // 通知分配用户
        Set<Long> userIds = rescueTaskAssignMapper.selectList(rescueTaskAssignMapper.queryUserIdByTask(taskId)).stream()
                .map(RescueTaskAssign::getUserId)
                .collect(Collectors.toSet()); // 所有已分配用户
        List<User> users = userService.listById(userIds,
                User::getId, User::getEmail);
        Set<Long> newUserIds = assigns.stream()
                .map(RescueTaskAssign::getUserId)
                .collect(Collectors.toSet()); // 新被分配用户
        Set<String> newUserEmails = users.stream()
                .filter(user -> newUserIds.contains(user.getId()))
                .map(User::getEmail)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet()); // 新被分配用户邮箱
        eventPublisher.publishEvent( // 邮箱
                NotificationEvent.mail("任务分配通知", "您已被分配任务：" + buildALabel(task.getSummary(), taskId), newUserEmails));
        eventPublisher.publishEvent( // 系统内
                NotificationEvent.system("任务分配通知", "您已被分配任务：" + task.getSummary(), buildTaskUrl(taskId), newUserIds));
        Set<Long> oldUserIds = users.stream()
                .map(User::getId)
                .filter(id -> !newUserIds.contains(id))
                .collect(Collectors.toSet());
        eventPublisher.publishEvent( // 系统内
                NotificationEvent.system("任务分配通知", "您参与的任务有新人加入啦：" + task.getSummary(), buildTaskUrl(taskId), oldUserIds));
        return users.stream().map(UserResponse::fromEntity).toList();
    }

    private String buildTaskUrl(Long taskId) {
        return "/task/tasks/" + taskId;
    }

    private String buildALabel(String text, Long taskId) {
        return "<a href=\"" + host + buildTaskUrl(taskId) + "\">" + text + "</a>";
    }

    @Autowired
    public void setServices(UserService userService) {
        this.userService = userService;
    }
}
