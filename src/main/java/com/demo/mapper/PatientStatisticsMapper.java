package com.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 患者统计Mapper接口
 */
@Mapper
public interface PatientStatisticsMapper {
    
    /**
     * 获取总患者数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总患者数量
     */
    @Select("SELECT COUNT(DISTINCT p.patient_id) FROM patient p " +
            "INNER JOIN interventiontime i ON p.patient_id = i.patient_id " +
            "WHERE i.admission_date BETWEEN #{startDate} AND #{endDate}")
    Long getTotalPatients(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    /**
     * 获取平均干预时间（分钟）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 平均干预时间
     */
    @Select("SELECT AVG(TIMESTAMPDIFF(MINUTE, " +
            "STR_TO_DATE(CONCAT(i.admission_date, ' ', i.admission_time), '%Y-%m-%d %H%i'), " +
            "STR_TO_DATE(CONCAT(i.leave_surgery_date, ' ', i.leave_surgery_time), '%Y-%m-%d %H%i'))) " +
            "FROM interventiontime i " +
            "WHERE i.admission_date BETWEEN #{startDate} AND #{endDate} " +
            "AND i.leave_surgery_date IS NOT NULL AND i.leave_surgery_time IS NOT NULL")
    Double getAverageInterventionTime(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    /**
     * 获取救治成功率（百分比）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 救治成功率
     */
    @Select("SELECT " +
            "CASE " +
            "WHEN COUNT(*) = 0 THEN 0 " +
            "ELSE (COUNT(CASE WHEN i.death = '否' OR i.death IS NULL THEN 1 END) * 100.0 / COUNT(*)) " +
            "END " +
            "FROM interventiontime i " +
            "WHERE i.admission_date BETWEEN #{startDate} AND #{endDate}")
    Double getSuccessRate(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    /**
     * 获取月度时间热力图数据
     * @param year 年份
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return 热力图数据
     */
    List<Map<String, Object>> getMonthlyTimeHeatmapData(@Param("year") Integer year,
                                                        @Param("startDate") String startDate,
                                                        @Param("endDate") String endDate,
                                                        @Param("season") Integer season, 
                                                        @Param("timePeriod") Integer timePeriod);
    
    /**
     * 获取创伤部位分析数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return 创伤部位分析数据
     */
    List<Map<String, Object>> getInjuryAnalysisData(@Param("startDate") String startDate, 
                                @Param("endDate") String endDate,
                                @Param("season") Integer season,
                                @Param("timePeriod") Integer timePeriod);
    
    /**
     * 获取ISS评分分布数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return ISS评分分布数据
     */
    List<Map<String, Object>> getISSScoreDistributionData(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    /**
     * 获取身体区域损伤数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 身体区域损伤数据
     */
    List<Map<String, Object>> getBodyRegionInjuryData(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    /**
     * 获取身体区域损伤旭日图数据
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param year 年份（可选）
     * @return 身体区域损伤旭日图数据
     */
    List<Map<String, Object>> getBodyRegionSunburstData(@Param("season") Integer season,
                                                         @Param("timePeriod") Integer timePeriod,
                                                         @Param("startDate") String startDate,
                                                         @Param("endDate") String endDate,
                                                         @Param("year") Integer year);
    
    /**
     * 获取干预时间效率数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 干预时间效率数据
     */
    List<Map<String, Object>> getInterventionTimeEfficiencyData(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    /**
     * 获取患者流向数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 患者流向数据
     */
    List<Map<String, Object>> getPatientFlowData(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    /**
     * 获取伤因分布数据
     * @param year 年份
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return 伤因分布数据
     */
    List<Map<String, Object>> getInjuryCauseDistributionData(@Param("year") Integer year,
                                                             @Param("startDate") String startDate,
                                                             @Param("endDate") String endDate,
                                                             @Param("season") Integer season,
                                                             @Param("timePeriod") Integer timePeriod);
    
    /**
     * 获取ISS分布数据（轻伤、重伤、严重伤）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return ISS分布数据
     */
    List<Map<String, Object>> getISSDistributionData(@Param("startDate") String startDate,
                                                     @Param("endDate") String endDate,
                                                     @Param("year") Integer year,
                                                     @Param("season") Integer season,
                                                     @Param("timePeriod") Integer timePeriod);
    
    /**
     * 获取GCS分布数据（意识清楚、轻度意识障碍、中度意识障碍、昏迷）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return GCS分布数据
     */
    List<Map<String, Object>> getGCSDistributionData(@Param("startDate") String startDate,
                                                      @Param("endDate") String endDate,
                                                      @Param("year") Integer year,
                                                      @Param("season") Integer season,
                                                      @Param("timePeriod") Integer timePeriod);
    
    /**
     * 获取RTS分布数据（0-4分）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return RTS分布数据
     */
    List<Map<String, Object>> getRTSDistributionData(@Param("startDate") String startDate,
                                                      @Param("endDate") String endDate,
                                                      @Param("year") Integer year,
                                                      @Param("season") Integer season,
                                                      @Param("timePeriod") Integer timePeriod);
    
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
    List<Map<String, Object>> getPopulationBodyHeatmapData(@Param("startDate") String startDate,
                                                           @Param("endDate") String endDate,
                                                           @Param("year") Integer year,
                                                           @Param("season") Integer season,
                                                           @Param("timePeriod") Integer timePeriod,
                                                           @Param("ageGroup") Integer ageGroup,
                                                           @Param("gender") Integer gender,
                                                           @Param("severity") Integer severity);
}
