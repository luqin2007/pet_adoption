package com.example.backend.util;

import lombok.Getter;

/**
 * 服务器错误信息
 */
@Getter
public class ServiceException extends RuntimeException {

    /*
    异常码
     */
    public static final int E_REQUEST = 400; // 语法错误
    public static final int E_TOKEN = 401; // Token
    public static final int E_AUTH = 403; // 权限
    public static final int E_NOT_FOUND = 404;
    public static final int E_CONFLICT = 409; // 数据已存在
    public static final int E_INVALIDATE = 422; // 请求可被解析，但拒绝请求/校验错误
    public static final int E_SYSTEM = 503; // 系统错误
    public static final int E_UNAVAILABLE = 503; // 服务暂时不可用
    public static final int E_TIMEOUT = 504; // 服务超时

    private final int code;

    protected ServiceException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    protected ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static ServiceException request(String message) {
        return new ServiceException(E_REQUEST, message);
    }

    public static ServiceException request(String message, Throwable cause) {
        return new ServiceException(E_REQUEST, message, cause);
    }

    public static ServiceException token(String message) {
        return new ServiceException(E_TOKEN, message);
    }

    public static ServiceException token(String message, Throwable cause) {
        return new ServiceException(E_TOKEN, message, cause);
    }

    public static ServiceException auth(String message) {
        return new ServiceException(E_AUTH, message);
    }

    public static ServiceException auth(String message, Throwable cause) {
        return new ServiceException(E_AUTH, message, cause);
    }

    public static ServiceException notFound(String message) {
        return new ServiceException(E_NOT_FOUND, message);
    }

    public static ServiceException notFound(String message, Throwable cause) {
        return new ServiceException(E_NOT_FOUND, message, cause);
    }

    public static ServiceException conflict(String message) {
        return new ServiceException(E_CONFLICT, message);
    }

    public static ServiceException conflict(String message, Throwable cause) {
        return new ServiceException(E_CONFLICT, message, cause);
    }

    public static ServiceException invalidate(String message) {
        return new ServiceException(E_INVALIDATE, message);
    }

    public static ServiceException invalidate(String message, Throwable cause) {
        return new ServiceException(E_INVALIDATE, message, cause);
    }

    public static ServiceException system(String message) {
        return new ServiceException(E_SYSTEM, message);
    }

    public static ServiceException system(String message, Throwable cause) {
        return new ServiceException(E_SYSTEM, message, cause);
    }

    public static ServiceException unavailable(String message) {
        return new ServiceException(E_UNAVAILABLE, message);
    }

    public static ServiceException unavailable(String message, Throwable cause) {
        return new ServiceException(E_UNAVAILABLE, message, cause);
    }
}
