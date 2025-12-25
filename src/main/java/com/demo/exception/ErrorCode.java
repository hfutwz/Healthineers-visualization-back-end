package com.demo.exception;

/**
 * 错误码枚举类
 * 定义系统中常见的错误码和错误信息
 */
public enum ErrorCode {
    
    // 通用错误码 1000-1999
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(1001, "参数错误"),
    PARAM_MISSING(1002, "缺少必要参数"),
    PARAM_INVALID(1003, "参数格式不正确"),
    
    // 业务错误码 2000-2999
    PATIENT_NOT_FOUND(2001, "患者不存在"),
    PATIENT_ID_INVALID(2002, "患者ID无效"),
    DATA_NOT_FOUND(2003, "数据不存在"),
    DATA_ALREADY_EXISTS(2004, "数据已存在"),
    
    // 文件操作错误码 3000-3999
    FILE_UPLOAD_FAILED(3001, "文件上传失败"),
    FILE_NOT_FOUND(3002, "文件不存在"),
    FILE_TYPE_INVALID(3003, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(3004, "文件大小超出限制"),
    FILE_READ_ERROR(3005, "文件读取失败"),
    FILE_WRITE_ERROR(3006, "文件写入失败"),
    EXCEL_IMPORT_FAILED(3007, "Excel导入失败"),
    EXCEL_FORMAT_ERROR(3008, "Excel格式错误"),
    
    // 数据库操作错误码 4000-4999
    DATABASE_ERROR(4001, "数据库操作失败"),
    DATABASE_CONNECTION_ERROR(4002, "数据库连接失败"),
    SQL_EXECUTION_ERROR(4003, "SQL执行失败"),
    DATA_INTEGRITY_ERROR(4004, "数据完整性错误"),
    
    // 系统错误码 5000-5999
    SYSTEM_ERROR(5001, "系统内部错误"),
    SERVICE_UNAVAILABLE(5002, "服务不可用"),
    TIMEOUT_ERROR(5003, "请求超时"),
    UNKNOWN_ERROR(5999, "未知错误");
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误信息
     */
    private final String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

