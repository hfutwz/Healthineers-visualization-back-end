package com.demo.config;

import com.demo.dto.Result;
import com.demo.exception.BusinessException;
import com.demo.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中所有异常，返回友好的错误信息
 * 
 * @author System
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常 - @Valid 注解校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", errorMessage);
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), "参数校验失败: " + errorMessage);
    }

    /**
     * 处理参数绑定异常 - 表单数据绑定失败
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", errorMessage);
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), "参数绑定失败: " + errorMessage);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少必要参数: {}", e.getParameterName());
        return Result.fail(ErrorCode.PARAM_MISSING.getCode(), 
                String.format("缺少必要参数: %s", e.getParameterName()));
    }

    /**
     * 处理HTTP消息不可读异常（JSON格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误: {}", e.getMessage());
        String message = "请求体格式错误";
        if (e.getMessage() != null && e.getMessage().contains("JSON")) {
            message = "JSON格式错误，请检查请求体格式";
        }
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), message);
    }

    /**
     * 处理HTTP请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("HTTP方法不支持: {}", e.getMethod());
        return Result.fail(405, String.format("不支持的HTTP方法: %s", e.getMethod()));
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件大小超出限制: {}", e.getMaxUploadSize());
        return Result.fail(ErrorCode.FILE_SIZE_EXCEEDED.getCode(), 
                String.format("文件大小超出限制，最大允许: %d 字节", e.getMaxUploadSize()));
    }

    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMultipartException(MultipartException e) {
        log.error("文件上传异常", e);
        return Result.fail(ErrorCode.FILE_UPLOAD_FAILED.getCode(), "文件上传失败: " + e.getMessage());
    }

    /**
     * 处理IO异常
     */
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleIOException(IOException e) {
        log.error("IO异常", e);
        return Result.fail(ErrorCode.FILE_READ_ERROR.getCode(), "文件操作失败: " + e.getMessage());
    }

    /**
     * 处理SQL异常
     */
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleSQLException(SQLException e) {
        log.error("SQL异常: SQLState={}, ErrorCode={}", e.getSQLState(), e.getErrorCode(), e);
        String message = "数据库操作失败";
        // 根据SQL错误码返回更友好的错误信息
        if (e.getErrorCode() == 1062) {
            message = "数据重复，违反唯一性约束";
        } else if (e.getErrorCode() == 1451 || e.getErrorCode() == 1452) {
            message = "数据关联错误，存在外键约束";
        } else if (e.getErrorCode() == 1048) {
            message = "必填字段不能为空";
        }
        return Result.fail(ErrorCode.SQL_EXECUTION_ERROR.getCode(), message);
    }

    /**
     * 处理数据访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleDataAccessException(DataAccessException e) {
        log.error("数据访问异常", e);
        return Result.fail(ErrorCode.DATABASE_ERROR.getCode(), "数据库操作失败: " + e.getMessage());
    }

    /**
     * 处理数据完整性违反异常（外键约束、唯一性约束等）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("数据完整性错误", e);
        String message = "数据完整性错误";
        if (e.getMessage() != null) {
            if (e.getMessage().contains("Duplicate entry")) {
                message = "数据重复，违反唯一性约束";
            } else if (e.getMessage().contains("foreign key constraint")) {
                message = "数据关联错误，存在外键约束";
            } else if (e.getMessage().contains("cannot be null")) {
                message = "必填字段不能为空";
            }
        }
        return Result.fail(ErrorCode.DATA_INTEGRITY_ERROR.getCode(), message);
    }

    /**
     * 处理重复键异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据重复", e);
        return Result.fail(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "数据已存在，不能重复添加");
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "系统内部错误：空指针异常");
    }

    /**
     * 处理类型转换异常
     */
    @ExceptionHandler(ClassCastException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleClassCastException(ClassCastException e) {
        log.error("类型转换异常", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "类型转换错误: " + e.getMessage());
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常: {}", e.getMessage());
        return Result.fail(ErrorCode.PARAM_ERROR.getCode(), "参数错误: " + e.getMessage());
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleIllegalStateException(IllegalStateException e) {
        log.error("非法状态异常", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "系统状态错误: " + e.getMessage());
    }

    /**
     * 处理数组越界异常
     */
    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleArrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException e) {
        log.error("数组越界异常", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "数组越界错误");
    }

    /**
     * 处理运行时异常（兜底处理）
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        // 如果是已知的异常类型，返回更友好的错误信息
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = "服务器内部错误";
        }
        return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), message);
    }

    /**
     * 处理所有其他异常（最终兜底）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e) {
        log.error("未知异常", e);
        return Result.fail(ErrorCode.UNKNOWN_ERROR.getCode(), 
                "系统发生未知错误，请联系管理员");
    }
}

