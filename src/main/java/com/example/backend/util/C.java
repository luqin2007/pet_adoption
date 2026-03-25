package com.example.backend.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class C {

    /*
    用户角色
     */
    // 爱心人士
    public static final String USER_ROLE_NORMAL = "ROLE_NORMAL";
    // 志愿者
    public static final String USER_ROLE_VOLUNTEER = "ROLE_VOLUNTEER";
    // 救助站工作人员
    public static final String USER_ROLE_WORKER = "ROLE_WORKER";
    // 捐赠者
    public static final String USER_ROLE_DONOR = "ROLE_DONOR";
    // 兽医
    public static final String USER_ROLE_DOCTOR = "ROLE_DOCTOR";
    // 超级管理员
    public static final String USER_ROLE_ADMIN = "ROLE_ADMIN";
    public static final int USER_ROLE_MASK_NORMAL = 0x0;
    public static final int USER_ROLE_MASK_VOLUNTEER = 0x1;
    public static final int USER_ROLE_MASK_WORKER = 0x2;
    public static final int USER_ROLE_MASK_DONOR = 0x4;
    public static final int USER_ROLE_MASK_DOCTOR = 0x8;
    public static final int USER_ROLE_MASK_ADMIN = 0x10;
    public static final Set<String> USER_ROLES = Set.of(
            USER_ROLE_NORMAL, USER_ROLE_VOLUNTEER, USER_ROLE_WORKER,
            USER_ROLE_DONOR, USER_ROLE_DOCTOR, USER_ROLE_ADMIN);
    public static final int USER_ROLE_MASK_ALL = USER_ROLE_MASK_VOLUNTEER | USER_ROLE_MASK_WORKER |
            USER_ROLE_MASK_DONOR | USER_ROLE_MASK_DOCTOR | USER_ROLE_MASK_ADMIN;
    public static final int
            USER_ROLE_MASK_MIN = USER_ROLE_MASK_NORMAL,
            USER_ROLE_MASK_MAX = USER_ROLE_MASK_ALL;
    public static final Map<String, Integer> USER_ROLE_MASK_MAP = Map.of(
            USER_ROLE_NORMAL, USER_ROLE_MASK_ALL, // Match ALL
            USER_ROLE_VOLUNTEER, USER_ROLE_MASK_VOLUNTEER,
            USER_ROLE_WORKER, USER_ROLE_MASK_WORKER | USER_ROLE_MASK_VOLUNTEER,
            USER_ROLE_DONOR, USER_ROLE_MASK_DONOR,
            USER_ROLE_DOCTOR, USER_ROLE_MASK_DOCTOR,
            USER_ROLE_ADMIN, USER_ROLE_MASK_ADMIN);
    public static final Map<Integer, List<String>> USER_MASK_ROLE_MAP = Map.of(
            USER_ROLE_MASK_NORMAL, List.of(USER_ROLE_NORMAL),
            USER_ROLE_MASK_VOLUNTEER, List.of(USER_ROLE_NORMAL, USER_ROLE_VOLUNTEER),
            USER_ROLE_MASK_WORKER, List.of(USER_ROLE_NORMAL, USER_ROLE_WORKER, USER_ROLE_VOLUNTEER),
            USER_ROLE_MASK_DONOR, List.of(USER_ROLE_NORMAL, USER_ROLE_DONOR),
            USER_ROLE_MASK_DOCTOR, List.of(USER_ROLE_NORMAL, USER_ROLE_DOCTOR),
            USER_ROLE_MASK_ADMIN, new ArrayList<>(USER_ROLES));
    public static final Map<Integer, Integer> USER_ROLE_REZIP_MAP = Map.of(
            USER_ROLE_MASK_VOLUNTEER, USER_ROLE_MASK_VOLUNTEER,
            USER_ROLE_MASK_WORKER, USER_ROLE_MASK_WORKER | USER_ROLE_MASK_VOLUNTEER,
            USER_ROLE_MASK_DONOR, USER_ROLE_MASK_DONOR,
            USER_ROLE_MASK_DOCTOR, USER_ROLE_MASK_DOCTOR,
            USER_ROLE_MASK_ADMIN, USER_ROLE_MASK_ALL);

    /*
    宠物状态
     */
    // 待审核
    public static final int PET_STATUS_WAITING = 1;
    // 审核未通过
    public static final int PET_STATUS_AGAINST = 2;
    // 已审核，查找中
    public static final int PET_STATUS_FINDING = 3;
    // 已找到，无法救助或已死亡
    public static final int PET_STATUS_DIED = 4;
    // 已超时，放弃救助
    public static final int PET_STATUS_TIMEOUT = 5;
    // 已收容
    public static final int PET_STATUS_SHELTERED = 6;
    // 已完成体检，可领养
    public static final int PET_STATUS_HEALTH = 7;
    // 已领养
    public static final int PET_STATUS_ADOPTED = 8;
    public static final int PET_STATUS_MIN = PET_STATUS_WAITING, PET_STATUS_MAX = PET_STATUS_ADOPTED;

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
    public static final int
            RESCUE_TASK_STATUS_MIN = RESCUE_TASK_STATUS_CREATED,
            RESCUE_TASK_STATUS_MAX = RESCUE_TASK_STATUS_DISCARDED;

    /*
    救助任务类型
     */
    // 发现流浪宠物
    public static final Integer RESCUE_TASK_TYPE_FIND = 1;
    // 医疗救助
    public static final Integer RESCUE_TASK_TYPE_MEDICAL = 2;
    // 其他
    public static final Integer RESCUE_TASK_TYPE_OTHER = 3;
    public static final int
            RESCUE_TASK_TYPE_MIN = RESCUE_TASK_TYPE_FIND,
            RESCUE_TASK_TYPE_MAX = RESCUE_TASK_TYPE_OTHER;

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

    /*
    诊疗流程
     */
    // 待接诊
    public static final Integer MEDICAL_VISIT_STATUS_WAITING = 1;
    // 接诊中
    public static final Integer MEDICAL_VISIT_STATUS_PROCESSING = 2;
    // 待缴费
    public static final Integer MEDICAL_VISIT_STATUS_PAYING = 3;
    // 完成
    public static final Integer MEDICAL_VISIT_STATUS_COMPLETED = 4;
    // 取消
    public static final Integer MEDICAL_VISIT_STATUS_CANCELED = 5;

    /*
    诊疗类型
     */
    // 初诊
    public static final Integer MEDICAL_TYPE_FIRST = 1;
    // 复诊
    public static final Integer MEDICAL_TYPE_REVISIT = 2;
    // 急诊
    public static final Integer MEDICAL_TYPE_EMERGENCY = 3;
    // 体检
    public static final Integer MEDICAL_TYPE_EXAMINATION = 4;

    /*
    医疗检查类型
     */
    // 血常规
    public static final Integer EXAM_TYPE_BLOOD = 1;
    // 生物化学检查
    public static final Integer EXAM_TYPE_BIOCHEMISTRY = 2;
    // X 光检查
    public static final Integer EXAM_TYPE_XRAY = 3;
    // 超声检查
    public static final Integer EXAM_TYPE_ULTRASOUND = 4;
    // 粪便检查
    public static final Integer EXAM_TYPE_FECAL_EXAM = 5;
    // 尿检
    public static final Integer EXAM_TYPE_URINALYSIS = 6;
    // 传染与免疫检查
    public static final Integer EXAM_TYPE_INFECTION = 7;
    // 细胞与病理学检查
    public static final Integer EXAM_TYPE_CELL = 8;
    // 内分泌
    public static final Integer EXAM_TYPE_SEMINAL = 9;
    // 其他检查
    public static final Integer EXAM_TYPE_OTHER = 10;
    public static final int
            EXAM_TYPE_MIN = EXAM_TYPE_BLOOD,
            EXAM_TYPE_MAX = EXAM_TYPE_OTHER;

    /*
    处方类型
     */
    // 药品
    public static final Integer ORDER_TYPE_MEDICINE = 1;
    // 手术
    public static final Integer ORDER_TYPE_SURGERY = 2;
    // 检查
    public static final Integer ORDER_TYPE_EXAMINATION = 3;
    // 其他
    public static final Integer ORDER_TYPE_OTHER = 4;
    public static final int ORDER_TYPE_MIN = ORDER_TYPE_MEDICINE, ORDER_TYPE_MAX = ORDER_TYPE_OTHER;

    /*
    驱虫药类型
     */
    // 内驱
    public static final Integer DEWORMER_TYPE_INTERNAL = 1;
    // 外驱
    public static final Integer DEWORMER_TYPE_EXTERNAL = 2;
    // 其他
    public static final Integer DEWORMER_TYPE_OTHER = 3;
    public static final int DEWORMER_TYPE_MIN = DEWORMER_TYPE_INTERNAL, DEWORMER_TYPE_MAX = DEWORMER_TYPE_OTHER;

    /**
     * 康复计划状态
     */
    // 生效中
    public static final Integer REHAB_PLAN_STATUS_ACTIVE = 1;
    // 废弃
    public static final Integer REHAB_PLAN_STATUS_DISCARD = 2;
    // 已完成
    public static final Integer REHAB_PLAN_STATUS_COMPLETED = 3;
    public static final int
            REHAB_PLAN_STATUS_MIN = REHAB_PLAN_STATUS_ACTIVE,
            REHAB_PLAN_STATUS_MAX = REHAB_PLAN_STATUS_COMPLETED;

    /*
    文本类型
     */
    // 无
    public static final Integer TEXT_TYPE_NONE = 0;
    // 纯文本
    public static final Integer TEXT_TYPE_PLAIN = 1;
    // Json
    public static final Integer TEXT_TYPE_JSON = 2;
    // Markdown
    public static final Integer TEXT_TYPE_MARKDOWN = 3;
    public static final int
            TEXT_TYPE_MIN = TEXT_TYPE_NONE,
            TEXT_TYPE_MAX = TEXT_TYPE_MARKDOWN;

    /*
    关联实体类型 parentType
     */
    // 用户
    public static final String PARENT_USER = "avatar";
    // 流浪宠物
    public static final String PARENT_PET = "pet";
    // 救助任务
    public static final String PARENT_RESCUE_TASK = "rescue";
    // 医疗检查
    public static final String PARENT_EXAMINATION = "examination";
    // 治疗计划
    public static final String PARENT_TREATMENT_PLAN = "treatment";
    // 康复计划
    public static final String PARENT_REHAB_PLAN = "rehab";

    /*
    媒体类型
     */
    public static final Integer MEDIA_TYPE_IMAGE = 0;
    public static final Integer MEDIA_TYPE_VIDEO = 1;

    /*
    Redis 键模板
     */
    public static final String KEY_PASSWORD_RESET = "pet_adoption:user.forgetpwd.%s";
    public static final String KEY_MAIL_CODE = "pet_adoption:user.mailcode.%s";
    public static final String KEY_RESCUE_TASK = "pet_adoption:rescue_task.%s";
    public static final String KEY_RESCUE_TASK_MEDIA = "pet_adoption:rescue_task.media.%s";
    public static final String KEY_EXAMINATION = "pet_adoption:medical.exam.%s";
    public static final String KEY_EXAMINATION_FILE = "pet_adoption:medical.exam.file.%s";

    /*
    文本模板
     */
    public static final String MESSAGE_SEND_MAIL_CODE = "%s\n验证码 10min 内有效";
    public static final String MESSAGE_RESET_PWD = "请点击以下链接重置密码：\n<a>%s/user/reset?id=%s</a>\n链接在 10min 内有效";
}
