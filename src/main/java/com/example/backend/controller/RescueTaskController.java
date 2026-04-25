package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.service.RescueTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 救助任务管理模块<br>
 * - 上报信息功能 ( × × )<br>
 * ---- 开始上报: beginRescueTask ( √ × )<br>
 * ---- 信息上报: addRescueTask ( √ × )<br>
 * ---- 媒体上传: uploadRescueMedia ( √ × )<br>
 * ---- 媒体删除: deleteRescueMediaWhenAdd ( √ × )<br>
 * ---- 信息修改: updateRescueTask ( √ × )<br>
 * ---- 已上传媒体删除: deleteRescueMediaWhenUpdate ( √ × )<br>
 * ---- 信息删除: deleteRescueTask ( √ × )<br>
 * - 任务分配功能 ( √ × )<br>
 * ---- 任务查询: getRescueTask/getRescueTasks ( √ × )<br>
 * ---- 任务分配: assignRescueTask ( √ × )<br>
 * - 任务提醒 ( √ × )<br>
 * - 上报跟踪功能 ( √ × )<br>
 * ---- 上报任务结果: updateRescueTaskRecord ( √ × )<br>
 * ---- 获取任务进度: getRescueTaskRecords ( √ × )
 */
@Validated
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class RescueTaskController {

    private final RescueTaskService rescueTaskService;

    /**
     * 获取所有信息
     */
    @GetMapping({"", "/"})
    public Result<Page<RescueTaskResponse>> getRescueTasks(PageParams pageParams) {
        Page<RescueTaskResponse> response = rescueTaskService.getRescueTasks(pageParams);
        return Result.success(response);
    }

    /**
     * 准备上报新救助任务
     */
    @PutMapping({"", "/"})
    public Result<String> beginRescueTask() {
        String id = rescueTaskService.beginRescueTask();
        return Result.success(id);
    }

    /**
     * 提交救助任务
     */
    @PostMapping({"", "/"})
    public Result<RescueTaskResponse> addRescueTask(@Valid @RequestBody RescueTaskAddRequest request) {
        RescueTaskResponse response = rescueTaskService.addRescueTask(request);
        return Result.success(response);
    }

    /**
     * 信息上报时上传媒体
     */
    @PostMapping("/{_id}/uploads")
    public Result<String> uploadRescueTaskMedia(@PathVariable("_id") String uuid,
                                                @RequestParam("file") MultipartFile file) {
        String response = rescueTaskService.uploadRescueTaskMedia(uuid, file);
        return Result.success(response);
    }

    /**
     * 信息上报时删除媒体
     *
     * @param uuid      临时实体 id
     * @param mediaName 媒体文件名
     */
    @DeleteMapping("/{_id}/uploads/{file}")
    public Result<Void> deleteRescueMediaWhenAdd(@PathVariable("_id") String uuid,
                                                 @PathVariable("file") String mediaName) {
        rescueTaskService.deleteRescueTaskMedia(uuid, mediaName);
        return Result.success();
    }

    /**
     * 获取救助任务信息
     */
    @GetMapping("/{id}")
    public Result<RescueTaskResponse> getRescueTask(@PathVariable("id") Long taskId) {
        RescueTaskResponse response = rescueTaskService.getRescueTask(taskId);
        return Result.success(response);
    }

    /**
     * 更新救助任务信息
     */
    @PutMapping("/{id}")
    public Result<RescueTaskResponse> updateRescueTask(@PathVariable("id") Long taskId,
                                                       @Valid @RequestBody RescueTaskUpdateRequest request) {
        RescueTaskResponse response = rescueTaskService.updateRescueTask(taskId, request);
        return Result.success(response);
    }

    /**
     * 删除救助任务
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRescueTask(@PathVariable("id") Long taskId) {
        rescueTaskService.deleteRescueTask(taskId);
        return Result.success();
    }

    /**
     * 获取任务状态记录
     */
    @GetMapping("/{id}/status")
    public Result<RescueTaskRecordsResponse> getStatusRecords(@PathVariable("id") Long taskId) {
        RescueTaskRecordsResponse response = rescueTaskService.getRescueTaskRecords(taskId);
        return Result.success(response);
    }

    /**
     * 修改任务状态
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long taskId,
                                     @Valid @RequestBody RescueTaskRecordStatusUpdateRequest request) {
        rescueTaskService.updateRescueTaskStatus(taskId, request);
        return Result.success();
    }

    /**
     * 分配任务
     */
    @PostMapping("/{id}/assign")
    public Result<List<UserResponse>> assignRescueTask(@PathVariable("id") Long taskId,
                                                       @Valid @RequestBody IdsRequest request) {
        List<UserResponse> responses = rescueTaskService.assignRescueTask(taskId, request);
        return Result.success(responses);
    }

    /**
     * 删除媒体文件
     *
     * @param mediaId 媒体 id
     */
    @DeleteMapping("/{id}/media/{mid}")
    public Result<Void> deleteRescueMediaWhenUpdate(@PathVariable("id") Long taskId, @PathVariable("mid") Long mediaId) {
        rescueTaskService.deleteRescueTaskMedia(taskId, mediaId);
        return Result.success();
    }
}
