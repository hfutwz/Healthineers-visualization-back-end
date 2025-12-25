package com.demo.dto;

import lombok.Data;
import java.util.List;

/**
 * 关键事件统计DTO
 * 包含单个关键事件的正态分布统计信息
 */
@Data
public class KeyEventStatisticsDTO {
    /**
     * 事件名称（中文）
     */
    private String eventName;
    
    /**
     * 事件类型（英文key）
     */
    private String eventType;
    
    /**
     * 平均时间（分钟）
     */
    private Double meanTime;
    
    /**
     * 标准差
     */
    private Double standardDeviation;
    
    /**
     * 中位时间（分钟）
     */
    private Double medianTime;
    
    /**
     * 质控标准线（μ - 1σ，但不小于0）
     */
    private Double qualityControlLine;
    
    /**
     * 有效数据数量
     */
    private Integer validDataCount;
    
    /**
     * 正态分布曲线数据点（从μ-2σ到μ+2σ）
     * 格式：[[x1, y1], [x2, y2], ...]
     */
    private List<List<Double>> distributionCurve;
    
    /**
     * X轴范围（最小值，最大值）
     */
    private List<Double> xAxisRange;
    
    /**
     * 当前患者时间（分钟，仅当传入patientId时有效）
     */
    private Double currentPatientTime;
}

