package com.example.backend.mapper;

import com.example.backend.entity.UserSetting;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSettingMapper extends IBaseMapper<UserSetting> {

    default MPLambdaQuery<UserSetting> queryByUser(Long userId) {
        return lambdaQuery().eq(UserSetting::getUserId, userId);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.user_setting";
    }

    @Override
    default Class<UserSetting> getEntityClass() {
        return UserSetting.class;
    }
}
