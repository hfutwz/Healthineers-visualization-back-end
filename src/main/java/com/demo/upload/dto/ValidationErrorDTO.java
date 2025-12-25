package com.demo.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据验证错误DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorDTO {
    /**
     * Excel行号（从1开始）
     */
    private Integer row;
    
    /**
     * 患者ID
     */
    private Integer patientId;
    
    /**
     * 字段名
     */
    private String field;
    
    /**
     * 错误的值
     */
    private Object value;
    
    /**
     * 错误原因
     */
    private String message;
}

