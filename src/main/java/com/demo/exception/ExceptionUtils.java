package com.demo.exception;

/**
 * 异常处理工具类
 * 提供便捷的异常抛出方法
 */
public class ExceptionUtils {

    /**
     * 抛出业务异常
     * @param message 错误信息
     */
    public static void throwBusinessException(String message) {
        throw new BusinessException(message);
    }

    /**
     * 抛出业务异常
     * @param code 错误码
     * @param message 错误信息
     */
    public static void throwBusinessException(Integer code, String message) {
        throw new BusinessException(code, message);
    }

    /**
     * 抛出业务异常
     * @param errorCode 错误码枚举
     */
    public static void throwBusinessException(ErrorCode errorCode) {
        throw new BusinessException(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 抛出业务异常（带自定义消息）
     * @param errorCode 错误码枚举
     * @param customMessage 自定义错误信息
     */
    public static void throwBusinessException(ErrorCode errorCode, String customMessage) {
        throw new BusinessException(errorCode.getCode(), customMessage);
    }

    /**
     * 判断条件，如果不满足则抛出异常
     * @param condition 条件
     * @param message 错误信息
     */
    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new BusinessException(message);
        }
    }

    /**
     * 判断条件，如果不满足则抛出异常
     * @param condition 条件
     * @param errorCode 错误码枚举
     */
    public static void requireTrue(boolean condition, ErrorCode errorCode) {
        if (!condition) {
            throw new BusinessException(errorCode.getCode(), errorCode.getMessage());
        }
    }

    /**
     * 判断对象是否为null，如果为null则抛出异常
     * @param obj 对象
     * @param message 错误信息
     */
    public static void requireNotNull(Object obj, String message) {
        if (obj == null) {
            throw new BusinessException(message);
        }
    }

    /**
     * 判断对象是否为null，如果为null则抛出异常
     * @param obj 对象
     * @param errorCode 错误码枚举
     */
    public static void requireNotNull(Object obj, ErrorCode errorCode) {
        if (obj == null) {
            throw new BusinessException(errorCode.getCode(), errorCode.getMessage());
        }
    }

    /**
     * 判断字符串是否为空，如果为空则抛出异常
     * @param str 字符串
     * @param message 错误信息
     */
    public static void requireNotBlank(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(message);
        }
    }

    /**
     * 判断字符串是否为空，如果为空则抛出异常
     * @param str 字符串
     * @param errorCode 错误码枚举
     */
    public static void requireNotBlank(String str, ErrorCode errorCode) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(errorCode.getCode(), errorCode.getMessage());
        }
    }
}

