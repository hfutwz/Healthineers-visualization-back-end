package com.demo.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 患者统计数据DTO
 * 包含总患者数、日均患者数、平均干预时间、死亡人数等统计信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientStatisticsDTO {
    /**
     * 总患者数量
     */
    private Long totalPatients;
    
    /**
     * 日均患者数量
     */
    private Double averagePatientsPerDay;
    
    /**
     * 平均干预时间（分钟）
     */
    private Double averageInterventionTime;
    
    /**
     * 死亡人数
     */
    private Long deathCount;
    
    /**
     * 统计时间范围开始日期
     */
    private String startDate;
    
    /**
     * 统计时间范围结束日期
     */
    private String endDate;
    
    /**
     * 统计的天数
     */
    private Integer totalDays;
}
