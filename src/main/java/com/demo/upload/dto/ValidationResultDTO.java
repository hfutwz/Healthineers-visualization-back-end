package com.demo.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据验证结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResultDTO {
    /**
     * 验证是否成功执行
     */
    private Boolean success;
    
    /**
     * 数据是否全部通过验证
     */
    private Boolean valid;
    
    /**
     * 错误数量
     */
    private Integer errorCount;
    
    /**
     * 错误列表
     */
    private List<ValidationErrorDTO> errors;
    
    /**
     * 消息
     */
    private String message;
}

