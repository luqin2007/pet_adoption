package com.example.backend.event;

import com.example.backend.entity.Location;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 发现新流浪宠物
 * - 向工作人员发送站内信：提醒审核新的流浪宠物记录
 *
 * @see com.example.backend.service.PetService#addPet(com.example.backend.dto.PetInfoAddRequest)
 */
public record PetAddEvent(Pet data, User user, Location location) implements INotifyEvent<Pet> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.PET_RECORD;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.pet_add.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.pet_add.content", data.getName("<未命名>"));
    }
}
