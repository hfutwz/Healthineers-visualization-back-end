package com.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTimeMinutes 自定义开始时间（分钟数，可选）
     * @param customEndTimeMinutes 自定义结束时间（分钟数，可选）
     * @return 总患者数量
     */
    Long getTotalPatients(@Param("startDate") String startDate, 
                         @Param("endDate") String endDate,
                         @Param("year") Integer year,
                         @Param("season") Integer season,
                         @Param("timePeriod") Integer timePeriod,
                         @Param("customStartTimeMinutes") Integer customStartTimeMinutes,
                         @Param("customEndTimeMinutes") Integer customEndTimeMinutes);
    
    /**
     * 获取干预时间原始数据（用于计算平均干预时间）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTimeMinutes 自定义开始时间（分钟数，可选）
     * @param customEndTimeMinutes 自定义结束时间（分钟数，可选）
     * @return 干预时间原始数据列表
     */
    List<com.demo.dto.InterventionTimeDTO> getAverageInterventionTime(@Param("startDate") String startDate, 
                                     @Param("endDate") String endDate,
                                     @Param("year") Integer year,
                                     @Param("season") Integer season,
                                     @Param("timePeriod") Integer timePeriod,
                                     @Param("customStartTimeMinutes") Integer customStartTimeMinutes,
                                     @Param("customEndTimeMinutes") Integer customEndTimeMinutes);
    
    /**
     * 获取死亡人数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTimeMinutes 自定义开始时间（分钟数，可选）
     * @param customEndTimeMinutes 自定义结束时间（分钟数，可选）
     * @return 死亡人数
     */
    Long getDeathCount(@Param("startDate") String startDate, 
                         @Param("endDate") String endDate,
                         @Param("year") Integer year,
                         @Param("season") Integer season,
                         @Param("timePeriod") Integer timePeriod,
                         @Param("customStartTimeMinutes") Integer customStartTimeMinutes,
                         @Param("customEndTimeMinutes") Integer customEndTimeMinutes);
    
    /**
     * 获取月度时间热力图数据
     * @param year 年份
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 热力图数据
     */
    List<Map<String, Object>> getMonthlyTimeHeatmapData(@Param("year") Integer year,
                                                        @Param("startDate") String startDate,
                                                        @Param("endDate") String endDate,
                                                        @Param("season") Integer season, 
                                                        @Param("timePeriod") Integer timePeriod,
                                                        @Param("customStartTime") String customStartTime,
                                                        @Param("customEndTime") String customEndTime);
    
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
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 身体区域损伤旭日图数据
     */
    List<Map<String, Object>> getBodyRegionSunburstData(@Param("season") Integer season,
                                                         @Param("timePeriod") Integer timePeriod,
                                                         @Param("startDate") String startDate,
                                                         @Param("endDate") String endDate,
                                                         @Param("year") Integer year,
                                                         @Param("customStartTime") String customStartTime,
                                                         @Param("customEndTime") String customEndTime);
    
    /**
     * 获取干预时间效率原始数据（用于计算时间段分布）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 干预时间原始数据列表
     */
    List<com.demo.dto.InterventionTimeDTO> getInterventionTimeEfficiencyData(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
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
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 伤因分布数据
     */
    List<Map<String, Object>> getInjuryCauseDistributionData(@Param("year") Integer year,
                                                             @Param("startDate") String startDate,
                                                             @Param("endDate") String endDate,
                                                             @Param("season") Integer season,
                                                             @Param("timePeriod") Integer timePeriod,
                                                             @Param("customStartTime") String customStartTime,
                                                             @Param("customEndTime") String customEndTime);
    
    /**
     * 获取ISS分布原始数据（业务逻辑在Service层处理）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTimeMinutes 自定义开始时间（分钟数，可选）
     * @param customEndTimeMinutes 自定义结束时间（分钟数，可选）
     * @return ISS原始数据列表
     */
    List<Map<String, Object>> getISSDistributionData(@Param("startDate") String startDate,
                                                     @Param("endDate") String endDate,
                                                     @Param("year") Integer year,
                                                     @Param("season") Integer season,
                                                     @Param("timePeriod") Integer timePeriod,
                                                     @Param("customStartTimeMinutes") Integer customStartTimeMinutes,
                                                     @Param("customEndTimeMinutes") Integer customEndTimeMinutes);
    
    /**
     * 获取GCS分布数据（意识清楚、轻度意识障碍、中度意识障碍、昏迷）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return GCS分布数据
     */
    List<Map<String, Object>> getGCSDistributionData(@Param("startDate") String startDate,
                                                      @Param("endDate") String endDate,
                                                      @Param("year") Integer year,
                                                      @Param("season") Integer season,
                                                      @Param("timePeriod") Integer timePeriod,
                                                      @Param("customStartTime") String customStartTime,
                                                      @Param("customEndTime") String customEndTime);
    
    /**
     * 获取RTS分布数据（0-4分）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return RTS分布数据
     */
    List<Map<String, Object>> getRTSDistributionData(@Param("startDate") String startDate,
                                                      @Param("endDate") String endDate,
                                                      @Param("year") Integer year,
                                                      @Param("season") Integer season,
                                                      @Param("timePeriod") Integer timePeriod,
                                                      @Param("customStartTime") String customStartTime,
                                                      @Param("customEndTime") String customEndTime);
    
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
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 人群身体热力图数据
     */
    List<Map<String, Object>> getPopulationBodyHeatmapData(@Param("startDate") String startDate,
                                                           @Param("endDate") String endDate,
                                                           @Param("year") Integer year,
                                                           @Param("season") Integer season,
                                                           @Param("timePeriod") Integer timePeriod,
                                                           @Param("ageGroup") Integer ageGroup,
                                                           @Param("gender") Integer gender,
                                                           @Param("severity") Integer severity,
                                                           @Param("customStartTime") String customStartTime,
                                                           @Param("customEndTime") String customEndTime);
    
    /**
     * 获取死亡患者ID列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 死亡患者ID列表
     */
    List<Integer> getDeathPatientIds(@Param("startDate") String startDate, 
                                     @Param("endDate") String endDate,
                                     @Param("year") Integer year,
                                     @Param("season") Integer season,
                                     @Param("timePeriod") Integer timePeriod,
                                     @Param("customStartTime") String customStartTime,
                                     @Param("customEndTime") String customEndTime);
    
    /**
     * 获取最久远的接诊日期（用于计算全部数据时的日均患者数）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return 最久远的接诊日期（格式：YYYY-MM-DD）
     */
    String getEarliestAdmissionDate(@Param("year") Integer year,
                                    @Param("season") Integer season,
                                    @Param("timePeriod") Integer timePeriod);
    
    /**
     * 根据伤因类型获取患者ID列表
     * @param injuryCauseCategory 伤因类型（0-交通伤，1-高坠伤，2-机械伤，3-跌倒，4-其他）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 患者ID列表
     */
    List<Integer> getInjuryCausePatientIds(@Param("injuryCauseCategory") Integer injuryCauseCategory,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("year") Integer year,
                                          @Param("season") Integer season,
                                          @Param("timePeriod") Integer timePeriod,
                                          @Param("customStartTime") String customStartTime,
                                          @Param("customEndTime") String customEndTime);
    
    /**
     * 根据ISS分段获取患者ID列表
     * @param issSegment ISS分段类型（light-轻度损伤ISS≤16，severe-重度损伤16<ISS≤25，critical-危害损伤ISS>25）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 患者ID列表
     */
    List<Integer> getISSSegmentPatientIds(@Param("issSegment") String issSegment,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("year") Integer year,
                                         @Param("season") Integer season,
                                         @Param("timePeriod") Integer timePeriod,
                                         @Param("customStartTimeMinutes") Integer customStartTimeMinutes,
                                         @Param("customEndTimeMinutes") Integer customEndTimeMinutes);
    
    /**
     * 根据GCS分段获取患者ID列表
     * @param gcsSegment GCS分段类型（clear-意识清楚15分，mild-轻度意识障碍12-14分，moderate-中度意识障碍9-11分，coma-昏迷3-8分）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTimeMinutes 自定义开始时间（分钟数，可选）
     * @param customEndTimeMinutes 自定义结束时间（分钟数，可选）
     * @return 患者ID列表
     */
    List<Integer> getGCSSegmentPatientIds(@Param("gcsSegment") String gcsSegment,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("year") Integer year,
                                         @Param("season") Integer season,
                                         @Param("timePeriod") Integer timePeriod,
                                         @Param("customStartTimeMinutes") Integer customStartTimeMinutes,
                                         @Param("customEndTimeMinutes") Integer customEndTimeMinutes);

    /**
     * 获取RTS评分患者ID列表
     */
    List<Integer> getRTSScorePatientIds(@Param("rtsScore") Integer rtsScore,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate,
                                        @Param("year") Integer year,
                                        @Param("season") Integer season,
                                        @Param("timePeriod") Integer timePeriod,
                                        @Param("customStartTimeMinutes") Integer customStartTimeMinutes,
                                        @Param("customEndTimeMinutes") Integer customEndTimeMinutes);
    
    /**
     * 根据身体部位获取患者ID列表
     * @param bodyPart 身体部位（head_neck-头颈部，face-面部，chest-胸部，abdomen-腹部，limbs-四肢，body-体表）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTimeMinutes 自定义开始时间（分钟数，可选）
     * @param customEndTimeMinutes 自定义结束时间（分钟数，可选）
     * @return 患者ID列表
     */
    List<Integer> getBodyPartPatientIds(@Param("bodyPart") String bodyPart,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate,
                                        @Param("year") Integer year,
                                        @Param("season") Integer season,
                                        @Param("timePeriod") Integer timePeriod,
                                        @Param("customStartTimeMinutes") Integer customStartTimeMinutes,
                                        @Param("customEndTimeMinutes") Integer customEndTimeMinutes);
    
    /**
     * 根据身体区域和严重程度获取患者ID列表
     * @param bodyRegion 身体区域（head_neck-头颈部，face-面部，chest-胸部，abdomen-腹部，limbs-四肢，body-体表）
     * @param severityLevel 严重程度（mild-轻度，moderate-中度，severe-重度，critical-无法医治）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTimeMinutes 自定义开始时间（分钟数，可选）
     * @param customEndTimeMinutes 自定义结束时间（分钟数，可选）
     * @return 患者ID列表
     */
    List<Integer> getBodyRegionSeverityPatientIds(@Param("bodyRegion") String bodyRegion,
                                                   @Param("severityLevel") String severityLevel,
                                                   @Param("startDate") String startDate,
                                                   @Param("endDate") String endDate,
                                                   @Param("year") Integer year,
                                                   @Param("season") Integer season,
                                                   @Param("timePeriod") Integer timePeriod,
                                                   @Param("customStartTimeMinutes") Integer customStartTimeMinutes,
                                                   @Param("customEndTimeMinutes") Integer customEndTimeMinutes);
}
