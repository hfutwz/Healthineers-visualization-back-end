package com.demo.dto;

import com.demo.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一响应结果类
 * 用于封装所有API的返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    /**
     * 操作是否成功
     */
    private Boolean success;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * 错误码（新增字段，用于更精确的错误定位）
     */
    private Integer code;
    
    /**
     * 返回数据
     */
    private Object data;
    
    /**
     * 总记录数（用于分页）
     */
    private Long total;

    /**
     * 成功响应 - 无数据
     */
    public static Result ok(){
        return new Result(true, null, ErrorCode.SUCCESS.getCode(), null, null);
    }
    
    /**
     * 成功响应 - 有数据
     */
    public static Result ok(Object data){
        return new Result(true, null, ErrorCode.SUCCESS.getCode(), data, null);
    }
    
    /**
     * 成功响应 - 列表数据（带总数）
     */
    public static Result ok(List<?> data, Long total){
        return new Result(true, null, ErrorCode.SUCCESS.getCode(), data, total);
    }
    
    /**
     * 失败响应 - 只有错误信息
     */
    public static Result fail(String errorMsg){
        return new Result(false, errorMsg, ErrorCode.FAIL.getCode(), null, null);
    }
    
    /**
     * 失败响应 - 错误码和错误信息
     */
    public static Result fail(Integer code, String errorMsg){
        return new Result(false, errorMsg, code, null, null);
    }
    
    /**
     * 成功响应 - 兼容旧代码
     */
    public static Result success(Object data){
        return new Result(true, null, ErrorCode.SUCCESS.getCode(), data, null);
    }
    
    /**
     * 错误响应 - 兼容旧代码
     */
    public static Result error(String errorMsg){
        return new Result(false, errorMsg, ErrorCode.FAIL.getCode(), null, null);
    }
    
    /**
     * 错误响应 - 使用ErrorCode枚举
     */
    public static Result error(ErrorCode errorCode){
        return new Result(false, errorCode.getMessage(), errorCode.getCode(), null, null);
    }
    
    /**
     * 错误响应 - 使用ErrorCode枚举和自定义消息
     */
    public static Result error(ErrorCode errorCode, String customMessage){
        return new Result(false, customMessage, errorCode.getCode(), null, null);
    }
}
