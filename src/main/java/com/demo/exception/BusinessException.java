package com.demo.exception;

import lombok.Getter;

/**
 * 自定义业务异常类
 * 用于处理业务逻辑中的异常情况
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private Integer code;
    
    /**
     * 错误信息
     */
    private String message;
    
    /**
     * 构造方法 - 只传入错误信息
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.message = message;
        this.code = 500; // 默认错误码
    }
    
    /**
     * 构造方法 - 传入错误码和错误信息
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 构造方法 - 传入错误信息和异常原因
     * @param message 错误信息
     * @param cause 异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = 500;
    }
    
    /**
     * 构造方法 - 传入错误码、错误信息和异常原因
     * @param code 错误码
     * @param message 错误信息
     * @param cause 异常原因
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}

