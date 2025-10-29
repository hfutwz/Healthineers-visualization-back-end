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
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return 患者统计数据
     */
    PatientStatisticsDTO getPatientStatistics(String startDate, String endDate, Integer year, Integer season, Integer timePeriod);
    
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
     * 获取身体区域损伤旭日图数据
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @return 身体区域损伤旭日图数据
     */
    List<Map<String, Object>> getBodyRegionSunburstData(Integer season, Integer timePeriod, String startDate, String endDate, Integer year);
    
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
    
    /**
     * 获取ISS分布数据（轻伤、重伤、严重伤）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return ISS分布数据
     */
    List<Map<String, Object>> getISSDistributionData(String startDate, String endDate, Integer year, Integer season, Integer timePeriod);
    
    /**
     * 获取GCS评分分布数据（意识清楚、轻度意识障碍、中度意识障碍、昏迷）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return GCS分布数据
     */
    List<Map<String, Object>> getGCSDistributionData(String startDate, String endDate, Integer year, Integer season, Integer timePeriod);
    
    /**
     * 获取RTS评分分布数据（0-4分）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return RTS分布数据
     */
    List<Map<String, Object>> getRTSDistributionData(String startDate, String endDate, Integer year, Integer season, Integer timePeriod);
    
    /**
     * 获取人群身体热力图数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param ageGroup 年龄组（可选，0-儿童，1-青年，2-中年，3-老年）
     * @param gender 性别（可选，0-男，1-女）
     * @param severity 严重程度（可选，0-轻伤，1-重伤，2-严重伤）
     * @return 人群身体热力图数据
     */
    List<Map<String, Object>> getPopulationBodyHeatmapData(String startDate, String endDate, Integer year, Integer season, Integer timePeriod, Integer ageGroup, Integer gender, Integer severity);
}
