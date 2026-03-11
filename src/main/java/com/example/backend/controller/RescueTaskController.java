package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.RescueTask;
import com.example.backend.service.RescueTaskService;
import com.example.backend.util.DbUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 救助任务管理模块
 * - 上报信息功能 ( × × )
 *   - 开始上报: beginRescueTask ( √ × )
 *   - 信息上报: addRescueTask ( √ × )
 *   - 媒体上传: updateRescueMedia ( √ × )
 *   - 媒体删除: deleteRescueMediaWhenAdd ( √ × )
 *   - 信息修改: updateRescueTask ( √ × )
 *   - 已上传媒体删除: deleteRescueMediaWhenUpdate ( √ × )
 *   - 信息建议 ( × × )
 *   - 信息删除: deleteRescueTask ( √ × )
 * - 任务分配功能 ( √ × )
 *   - 任务查询: getRescueTask/getRescueTasks ( √ × )
 *   - 任务分配: assignRescueTask ( √ × )
 *   - 任务提醒 ( √ × )
 * - 上报跟踪功能 ( √ × )
 *   - 上报任务结果: updateRescueTaskRecord ( √ × )
 *   - 获取任务进度: getRescueTaskRecords ( √ × )
 */
@Validated
@Controller
@RequestMapping("/task")
@RequiredArgsConstructor
public class RescueTaskController {

    private final RescueTaskService rescueTaskService;

    /**
     * 准备上报新救助任务
     */
    @PutMapping("/tasks")
    public Result<String> beginRescueTask() {
        String id = rescueTaskService.beginRescueTask();
        return Result.success(id);
    }

    /**
     * 获取救助信息列表
     */
    @PutMapping("/tasks")
    public Result<RescueTaskResponse> addRescueTask(@RequestBody RescueTaskAddRequest request) {
        RescueTaskResponse response = rescueTaskService.addRescueTask(request);
        return Result.success(response);
    }

    /**
     * 获取救助任务信息
     */
    @GetMapping("/tasks/{id}")
    public Result<RescueTaskResponse> getRescueTask(@PathVariable("id") Long taskId) {
        RescueTaskResponse response = rescueTaskService.getRescueTask(taskId);
        return Result.success(response);
    }

    /**
     * 获取所有信息
     */
    @GetMapping("/tasks")
    public Result<Page<RescueTaskResponse>> getRescueTasks(@RequestParam(defaultValue = "1") Integer current,
                                                           @RequestParam(defaultValue = "10") Integer size,
                                                           @RequestParam(defaultValue = "id") String sort,
                                                           @RequestParam(defaultValue = "ASC") String order) {
        Page<RescueTask> page = DbUtils.createPage(current, size, sort, order);
        Page<RescueTaskResponse> response = rescueTaskService.getRescueTasks(page);
        return Result.success(response);
    }

    /**
     * 更新救助任务信息
     */
    @PostMapping("/tasks/{id}")
    public Result<RescueTaskResponse> updateRescueTask(@PathVariable("id") Long taskId,
                                                       @RequestBody RescueTaskUpdateRequest request) {
        RescueTaskResponse response = rescueTaskService.updateRescueTask(taskId, request);
        return Result.success(response);
    }

    /**
     * 删除救助任务
     */
    @DeleteMapping("/tasks/{id}")
    public Result<Void> deleteRescueTask(@PathVariable("id") Long taskId) {
        rescueTaskService.deleteRescueTask(taskId);
        return Result.success();
    }

    /**
     * 上报任务结果
     */
    @PostMapping("/status/{id}")
    public Result<Void> updateRescueTaskRecord(@PathVariable("id") Long taskId, RescueTaskRecordRequest request) {
        rescueTaskService.updateRescueTaskRecord(taskId, request);
        return Result.success();
    }

    /**
     * 获取任务状态记录
     */
    @PostMapping("/status/{id}")
    public Result<RescueTaskRecordsResponse> getRescueTaskRecords(@PathVariable("id") Long taskId) {
        RescueTaskRecordsResponse response = rescueTaskService.getRescueTaskRecords(taskId);
        return Result.success(response);
    }

    /**
     * 分配任务
     */
    @PostMapping("/tasks/{id}/assign")
    public Result<List<UserResponse>> assignRescueTask(@PathVariable("id") Long taskId, IdsRequest request) {
        List<UserResponse> responses = rescueTaskService.assignRescueTask(taskId, request);
        return Result.success(responses);
    }

    /**
     * 信息上报时上传媒体
     */
    @PostMapping("/media/{id}")
    public Result<Long> updateRescueMedia(@PathVariable("id") String uuid, @RequestBody MultipartFile file) {
        Long id = rescueTaskService.uploadMediaFile(uuid, file);
        return Result.success(id);
    }

    /**
     * 信息上报时删除图片
     * @param uuid 临时实体 id
     * @param mediaId 媒体 id
     */
    @DeleteMapping("/media/{id}/{mid}")
    public Result<Void> deleteRescueMediaWhenAdd(@PathVariable("id") String uuid, @PathVariable("mid") Long mediaId) {
        rescueTaskService.deleteRescueMedia(uuid, mediaId);
        return Result.success();
    }

    /**
     * 删除媒体文件
     * @param mediaId 媒体 id
     */
    @DeleteMapping("/media/{id}")
    public Result<Void> deleteRescueMediaWhenUpdate(@PathVariable("id") Long mediaId) {
        rescueTaskService.deleteRescueMedia(mediaId);
        return Result.success();
    }
}
