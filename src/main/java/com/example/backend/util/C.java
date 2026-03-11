package com.example.backend.util;

import java.util.Map;
import java.util.Set;

public class C {

    /*
    用户角色
     */
    // 爱心人士
    public static final String ROLE_NORMAL = "ROLE_NORMAL";
    // 志愿者
    public static final String ROLE_VOLUNTEER = "ROLE_VOLUNTEER";
    // 救助站工作人员
    public static final String ROLE_WORKER = "ROLE_WORKER";
    // 捐赠者
    public static final String ROLE_DONOR = "ROLE_DONOR";
    // 兽医
    public static final String ROLE_VETERINARIAN = "ROLE_VETERINARIAN";
    // 超级管理员
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final Set<String> ALL_ROLES;
    public static final Map<String, Integer> MATCH_MASK_MAP;

    public static final int MASK_VOLUNTEER = 0x1;
    public static final int MASK_WORKER = 0x2;
    public static final int MASK_DONOR = 0x4;
    public static final int MASK_VETERINARIAN = 0x8;
    public static final int MASK_ADMIN = 0x10;

    /*
    宠物状态
     */
    // 待审核
    public static final int PET_STATUS_WAITING = 0;
    // 审核未通过
    public static final int PET_STATUS_NOT_APPROVED = 1;
    // 已审核，查找中
    public static final int PET_STATUS_FINDING = 2;
    // 已找到，无法救助或已死亡
    public static final int PET_STATUS_DIED = 3;
    // 已超时，放弃救助
    public static final int PET_STATUS_TIMEOUT = 4;
    // 已收容
    public static final int PET_STATUS_SHELTERED = 5;
    // 已完成体检，可领养
    public static final int PET_STATUS_HEALTH = 6;
    // 已领养
    public static final int PET_STATUS_ADOPTED = 7;

    /*
    关联实体类型 parentType
     */
    public static final String PARENT_USER_AVATAR = "avatar";
    public static final String PARENT_PET = "pet";
    public static final String PARENT_RESCUE_TASK = "rescue";

    /*
    媒体类型
     */
    public static final Integer MEDIA_TYPE_IMAGE = 0;
    public static final Integer MEDIA_TYPE_VIDEO = 1;

    /*
    救助任务状态
     */
    // 已创建
    public static final Integer RESCUE_TASK_STATUS_CREATED = 1;
    // 审核通过，之后无法修改，只能增加附加信息
    public static final Integer RESCUE_TASK_STATUS_APPROVED = 2;
    // 正在处理中
    public static final Integer RESCUE_TASK_STATUS_PROCESSING = 3;
    // 任务完成
    public static final Integer RESCUE_TASK_STATUS_COMPLETED = 4;
    // 已废弃
    public static final Integer RESCUE_TASK_STATUS_DISCARDED = 5;

    /*
    救助任务类型
     */
    // 发现流浪宠物
    public static final Integer RESCUE_TASK_TYPE_FIND = 0;
    // 医疗救助
    public static final Integer RESCUE_TASK_TYPE_MEDICAL = 1;
    // 其他
    public static final Integer RESCUE_TASK_TYPE_OTHER = 2;

    /*
    救助任务变更类型
     */
    // 创建任务
    public static final Integer RESCUE_TASK_ACTION_CREATE = 1;
    // 修改任务
    public static final Integer RESCUE_TASK_ACTION_UPDATE = 2;
    // 修改状态
    public static final Integer RESCUE_TASK_ACTION_STATUS = 3;
    // 产生子任务
    public static final Integer RESCUE_TASK_ACTION_FORK = 4;
    // 产生建议
    public static final Integer RESCUE_TASK_ACTION_SUGGEST = 5;

    /*
    Redis 键模板
     */
    public static final String KEY_PASSWORD_RESET = "pet_adoption:user.forgetpwd.%s";
    public static final String KEY_MAIL_CODE = "pet_adoption:user.mailcode.%s";
    public static final String KEY_RESCUE_TASK = "pet_adoption:rescue_task.%s";
    public static final String KEY_RESCUE_TASK_MEDIA = "pet_adoption:rescue_task.media.%s";
    public static final String KEY_RESCUE_TASK_MEDIA_COUNT = "pet_adoption:rescue_task.count.%s";

    /*
    文本模板
     */
    public static final String MESSAGE_SEND_MAIL_CODE = "%s\n验证码 10min 内有效";
    public static final String MESSAGE_RESET_PWD = "请点击以下链接重置密码：\n<a>%s/user/reset?id=%s</a>\n链接在 10min 内有效";

    static {
        // 用户角色
        ALL_ROLES = Set.of(
                ROLE_NORMAL,
                ROLE_VOLUNTEER,
                ROLE_WORKER,
                ROLE_DONOR,
                ROLE_VETERINARIAN,
                ROLE_ADMIN);
        MATCH_MASK_MAP = Map.of(
                ROLE_NORMAL, 0x1F, // Match ALL
                ROLE_VOLUNTEER, MASK_VOLUNTEER,
                ROLE_WORKER, MASK_WORKER,
                ROLE_DONOR, MASK_DONOR,
                ROLE_VETERINARIAN, MASK_VETERINARIAN,
                ROLE_ADMIN, MASK_ADMIN);
    }
}
