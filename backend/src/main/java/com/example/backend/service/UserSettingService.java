package com.example.backend.service;

import com.example.backend.entity.UserSetting;
import com.example.backend.mapper.UserSettingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserSettingService extends BaseService<UserSettingMapper, UserSetting> {

    @Transactional
    public UserSetting getOrCreate(Long userId) {
        UserSetting s = baseMapper.queryByUser(userId).one();
        if (s == null) {
            Date now = new Date();
            s = new UserSetting(null, userId, true, now, now);
            baseMapper.insert(s);
        }
        return s;
    }

    @Transactional
    public void setConfig(Long userId, UserSetting request) {
        UserSetting settings = getOrCreate(userId);
        settings.accept(request);
        updateById(settings);
    }
}
