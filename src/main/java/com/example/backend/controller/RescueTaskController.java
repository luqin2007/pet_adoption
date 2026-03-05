package com.example.backend.controller;

import com.example.backend.service.RescueTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 救助任务管理模块
 * - 上报信息功能 ( × × )
 *   - 信息上报 ( × × )
 *   - 信息修改 ( × × )
 *   - 信息补充 ( × × )
 *   - 信息删除 ( × × )
 * - 任务分配功能 ( × × )
 *   - 任务查询 ( × × )
 *   - 任务分配 ( × × )
 *   - 任务提醒 ( × × )
 * - 上报跟踪功能 ( × × )
 *   - 下一环任务 ( × × )
 *   - 上报任务结果 ( × × )
 */
@Validated
@Controller
@RequestMapping("/task")
@RequiredArgsConstructor
public class RescueTaskController {

    private final RescueTaskService rescueTaskService;

    public void addRescueTask() {

    }
}
