package com.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 时间线事件DTO
 */
@Data
public class TimelineEventDTO {
    /**
     * 事件名称
     */
    private String eventName;
    
    /**
     * 完整时间（包含日期）
     */
    private LocalDateTime eventTime;
    
    /**
     * 事件类型：key/non_key
     */
    private String eventType;
    
    /**
     * 事件分组
     */
    private String eventGroup;
    
    /**
     * 事件描述
     */
    private String description;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 颜色
     */
    private String color;
    
    /**
     * 备注信息
     */
    private String notes;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 患者去向（仅用于离室事件）
     */
    private String destination;
}
