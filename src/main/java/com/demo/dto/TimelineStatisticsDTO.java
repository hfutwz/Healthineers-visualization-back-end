package com.demo.dto;

import lombok.Data;
import java.util.List;

/**
 * 时间线统计DTO
 */
@Data
public class TimelineStatisticsDTO {
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 平均时间（分钟）
     */
    private Double meanTime;
    
    /**
     * 中位时间（分钟）
     */
    private Double medianTime;
    
    /**
     * 标准差
     */
    private Double standardDeviation;
    
    /**
     * 当前患者时间
     */
    private Double currentPatientTime;
    
    /**
     * 质控标准线
     */
    private Double qualityControlLine;
    
    /**
     * 分布曲线数据点
     */
    private List<Double> distributionPoints;
}
