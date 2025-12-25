package com.demo.upload.exception;

import java.util.Map;

/**
 * 数据验证异常
 * 用于在批量导入时，当验证失败时抛出，携带详细的错误信息
 */
public class DataValidationException extends RuntimeException {
    
    private final Map<String, Object> errorDetails;
    
    public DataValidationException(String message, Map<String, Object> errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }
    
    public Map<String, Object> getErrorDetails() {
        return errorDetails;
    }
}

