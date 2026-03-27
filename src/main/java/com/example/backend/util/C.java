package com.example.backend.util;

public class C {
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
