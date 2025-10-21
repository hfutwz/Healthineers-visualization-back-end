package com.demo.Service.impl;

import com.demo.dto.PatientStatisticsDTO;

import java.util.List;
import java.util.Map;

/**
 * 患者统计服务接口
 */
public interface IPatientStatisticsService {
    
    /**
     * 获取患者统计数据
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @return 患者统计数据
     */
    PatientStatisticsDTO getPatientStatistics(String startDate, String endDate);
    
    /**
     * 获取月度时间热力图数据
     * @param year 年份（可选）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return 月度时间热力图数据
     */
    List<Map<String, Object>> getMonthlyTimeHeatmapData(Integer year, String startDate, String endDate, Integer season, Integer timePeriod);
    
    /**
     * 获取创伤部位分析数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return 创伤部位分析数据
     */
    List<Map<String, Object>> getInjuryAnalysisData(String startDate, String endDate, Integer season, Integer timePeriod);
    
    /**
     * 获取ISS评分分布数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return ISS评分分布数据
     */
    List<Map<String, Object>> getISSScoreDistributionData(String startDate, String endDate);
    
    /**
     * 获取身体区域损伤数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 身体区域损伤数据
     */
    List<Map<String, Object>> getBodyRegionInjuryData(String startDate, String endDate);
    
    /**
     * 获取干预时间效率数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 干预时间效率数据
     */
    List<Map<String, Object>> getInterventionTimeEfficiencyData(String startDate, String endDate);
    
    /**
     * 获取患者流向数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 患者流向数据
     */
    List<Map<String, Object>> getPatientFlowData(String startDate, String endDate);
    
    /**
     * 获取伤因分布数据
     * @param year 年份（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return 伤因分布数据
     */
    List<Map<String, Object>> getInjuryCauseDistributionData(Integer year, String startDate, String endDate, Integer season, Integer timePeriod);
}
