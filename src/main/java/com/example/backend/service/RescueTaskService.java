package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.RescueTask;
import com.example.backend.mapper.RescueTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RescueTaskService extends ServiceImpl<RescueTaskMapper, RescueTask> {
}
