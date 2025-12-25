package com.demo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 时间段分组统计DTO - 用于返回分组统计结果
 */
@Data
public class HourlyGroupStatisticsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 组索引（从0开始）
     */
    private Integer groupIndex;
    
    /**
     * 组标签（如 "0-1,1-2,2-3"）
     */
    private String groupLabel;
    
    /**
     * 该组的患者总数
     */
    private Integer count;
    
    /**
     * 该组包含的小时列表（用于前端展示）
     */
    private String hoursDisplay;
}

