package com.example.backend.event;

import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;
import com.example.backend.util.ServiceException;

public interface INotifyEvent<T> extends IEvent<T> {

    NoticeSource getSource();

    String buildNotifyTitle(LangHelper langHelper);

    /**
     * 构建站内通知内容。
     *
     * @param args 仅用于事件本身无法直接拿到、需要调用方额外查询后补充的参数，
     *             例如 {@code AdoptAddEvent} 需要额外查询宠物名；事件 {@code data}、
     *             关联状态记录、当前用户等可直接从事件中取得的信息不应再从这里重复传入
     */
    String buildNotifyContent(LangHelper langHelper, Object... args);

    default String buildMailTitle(LangHelper langHelper) {
        throw ServiceException.system("Never HERE!");
    }

    /**
     * 构建邮件内容。
     *
     * @param args 语义同 {@link #buildNotifyContent(LangHelper, Object...)}
     */
    default String buildMailContent(LangHelper langHelper, Object... args) {
        throw ServiceException.system("Never HERE!");
    }
}
