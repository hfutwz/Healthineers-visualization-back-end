package com.demo.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 时间段分组DTO - 用于接收前端传递的分组信息
 */
@Data
public class HourlyGroupDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 组索引（从0开始）
     */
    private Integer groupIndex;
    
    /**
     * 该组包含的小时列表（0-23）
     */
    private List<Integer> hours;
}

