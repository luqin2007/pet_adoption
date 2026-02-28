package com.example.backend.controller;

import com.example.backend.service.RescueTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 */
@Validated
@Controller
@RequestMapping("/task")
@RequiredArgsConstructor
public class RescueTaskController {

    private final RescueTaskService rescueTaskService;
}
