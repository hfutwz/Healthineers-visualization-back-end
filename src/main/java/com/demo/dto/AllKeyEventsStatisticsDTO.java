package com.demo.dto;

import lombok.Data;
import java.util.List;

/**
 * 所有关键事件统计DTO
 * 包含所有关键事件的正态分布统计信息和错误数据
 */
@Data
public class AllKeyEventsStatisticsDTO {
    /**
     * 所有关键事件的统计信息
     */
    private List<KeyEventStatisticsDTO> eventStatistics;
    
    /**
     * 错误数据列表
     */
    private List<DataErrorDTO> errorData;
    
    /**
     * 错误数据总数
     */
    private Integer errorCount;
}

