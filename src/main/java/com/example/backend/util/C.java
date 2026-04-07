package com.example.backend.util;

/**
 * 常数类
 */
public class C {
    /*
    Redis 键模板
     */
    public static final String KEY_INVALID_TOKEN = "pet_adoption.invalid_token.%s";
    public static final String KEY_PASSWORD_RESET = "pet_adoption:user.forgetpwd.%s";
    public static final String KEY_MAIL_CODE = "pet_adoption:user.mailcode.%s";
    public static final String KEY_RESCUE_TASK = "pet_adoption:rescue_task.%s";
    public static final String KEY_RESCUE_TASK_MEDIA = "pet_adoption:rescue_task.media.%s";
    public static final String KEY_EXAMINATION = "pet_adoption:medical.exam.%s";
    public static final String KEY_EXAMINATION_FILE = "pet_adoption:medical.exam.file.%s";
    public static final String KEY_DONATION = "pet_adoption:donation.%s";
    public static final String KEY_DONATION_FILE = "pet_adoption:donation.file.%s";

    /*
    Token 相关
     */
    public static final String TOKEN_CLAIM_TYPE_KEY = "tokenType";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    /*
    文本模板
     */
    public static final String MESSAGE_SEND_MAIL_CODE = "%s\n验证码 10min 内有效";
    public static final String MESSAGE_RESET_PWD = "请点击以下链接重置密码：\n<a>%s/user/reset?id=%s</a>\n链接在 10min 内有效";
}
