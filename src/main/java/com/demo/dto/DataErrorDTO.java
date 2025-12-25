package com.demo.dto;

import lombok.Data;

/**
 * 数据错误DTO
 * 用于记录异常数据（时间差超过48小时或为负数）
 */
@Data
public class DataErrorDTO {
    /**
     * 患者ID
     */
    private Integer patientId;
    
    /**
     * 事件名称
     */
    private String eventName;
    
    /**
     * 入室时间（格式：YYYY-MM-DD HH:mm）
     */
    private String admissionTime;
    
    /**
     * 关键事件发生时间（格式：YYYY-MM-DD HH:mm）
     */
    private String eventTime;
    
    /**
     * 时间差（分钟）
     */
    private Long timeDifferenceMinutes;
    
    /**
     * 错误原因
     */
    private String errorReason;
}

