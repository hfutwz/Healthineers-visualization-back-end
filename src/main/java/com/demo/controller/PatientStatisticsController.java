package com.demo.controller;

import com.demo.Service.impl.IPatientStatisticsService;
import com.demo.dto.PatientStatisticsDTO;
import com.demo.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 患者统计控制器
 */
@RestController
@RequestMapping("/api/patient-statistics")
@CrossOrigin(origins = "*")
public class PatientStatisticsController {
    
    @Autowired
    private IPatientStatisticsService patientStatisticsService;
    
    /**
     * 获取患者统计数据
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param year 年份（可选）
     * @param timePeriod 时间段（可选，night-夜间，morning_peak-早高峰，noon_peak-午高峰，afternoon-下午，evening_peak-晚高峰，evening-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 患者统计数据
     */
    @GetMapping("/statistics")
    public Result getPatientStatistics(@RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate,
                                      @RequestParam(required = false) String year,
                                      @RequestParam(required = false) String timePeriod,
                                      @RequestParam(required = false) String customStartTime,
                                      @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty()) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            // 处理时间段参数
            Integer timePeriodInt = null;
            if (timePeriod != null && !timePeriod.equals("all") && !timePeriod.isEmpty()) {
                switch (timePeriod) {
                    case "night": timePeriodInt = 0; break;
                    case "morning_peak": timePeriodInt = 1; break;
                    case "noon_peak": timePeriodInt = 2; break;
                    case "afternoon": timePeriodInt = 3; break;
                    case "evening_peak": timePeriodInt = 4; break;
                    case "evening": timePeriodInt = 5; break;
                }
            }
            
            PatientStatisticsDTO statistics = patientStatisticsService.getPatientStatistics(startDate, endDate, yearInt, null, timePeriodInt, customStartTime, customEndTime);
            return Result.ok(statistics);
        } catch (Exception e) {
            return Result.fail("获取患者统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取月度时间热力图数据
     * @param year 年份（可选，可以是 "all" 表示查询所有年份）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 热力图数据
     */
    @GetMapping("/monthly-heatmap")
    public Result getMonthlyTimeHeatmapData(@RequestParam(required = false) String year,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate,
                                           @RequestParam(required = false) Integer timePeriod,
                                           @RequestParam(required = false) String customStartTime,
                                           @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数：将 "all" 或 "NaN" 转换为 null
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty() && !year.equalsIgnoreCase("NaN")) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            Object heatmapData = patientStatisticsService.getMonthlyTimeHeatmapData(yearInt, startDate, endDate, null, timePeriod, customStartTime, customEndTime);
            return Result.ok(heatmapData);
        } catch (Exception e) {
            return Result.fail("获取月度时间热力图数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取创伤部位分析数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return 创伤部位分析数据
     */
    @GetMapping("/injury-analysis")
    public Result getInjuryAnalysisData(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate,
                                       @RequestParam(required = false) Integer season,
                                       @RequestParam(required = false) Integer timePeriod) {
        try {
            List<Map<String, Object>> analysisData = patientStatisticsService.getInjuryAnalysisData(startDate, endDate, season, timePeriod);
            return Result.ok(analysisData);
        } catch (Exception e) {
            return Result.fail("获取创伤部位分析数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取ISS评分分布数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return ISS评分分布数据
     */
    @GetMapping("/iss-score-distribution")
    public Result getISSScoreDistributionData(@RequestParam(required = false) String startDate,
                                              @RequestParam(required = false) String endDate) {
        try {
            List<Map<String, Object>> distributionData = patientStatisticsService.getISSScoreDistributionData(startDate, endDate);
            return Result.ok(distributionData);
        } catch (Exception e) {
            return Result.fail("获取ISS评分分布数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取身体区域损伤数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 身体区域损伤数据
     */
    @GetMapping("/body-region-injury")
    public Result getBodyRegionInjuryData(@RequestParam(required = false) String startDate,
                                         @RequestParam(required = false) String endDate) {
        try {
            List<Map<String, Object>> regionData = patientStatisticsService.getBodyRegionInjuryData(startDate, endDate);
            return Result.ok(regionData);
        } catch (Exception e) {
            return Result.fail("获取身体区域损伤数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取身体区域损伤旭日图数据
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选，可以是 "all" 表示查询所有年份）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 身体区域损伤旭日图数据
     */
    @GetMapping("/body-region-sunburst")
    public Result getBodyRegionSunburstData(@RequestParam(required = false) Integer timePeriod,
                                          @RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate,
                                          @RequestParam(required = false) String year,
                                          @RequestParam(required = false) String customStartTime,
                                          @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数：将 "all" 或 "NaN" 转换为 null
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty() && !year.equalsIgnoreCase("NaN")) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            List<Map<String, Object>> sunburstData = patientStatisticsService.getBodyRegionSunburstData(null, timePeriod, startDate, endDate, yearInt, customStartTime, customEndTime);
            return Result.ok(sunburstData);
        } catch (Exception e) {
            return Result.fail("获取身体区域损伤旭日图数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取干预时间效率数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 干预时间效率数据
     */
    @GetMapping("/intervention-time-efficiency")
    public Result getInterventionTimeEfficiencyData(@RequestParam(required = false) String startDate,
                                                   @RequestParam(required = false) String endDate) {
        try {
            List<Map<String, Object>> efficiencyData = patientStatisticsService.getInterventionTimeEfficiencyData(startDate, endDate);
            return Result.ok(efficiencyData);
        } catch (Exception e) {
            return Result.fail("获取干预时间效率数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取患者流向数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 患者流向数据
     */
    @GetMapping("/patient-flow")
    public Result getPatientFlowData(@RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate) {
        try {
            List<Map<String, Object>> flowData = patientStatisticsService.getPatientFlowData(startDate, endDate);
            return Result.ok(flowData);
        } catch (Exception e) {
            return Result.fail("获取患者流向数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取伤因分布数据
     * @param year 年份（可选，可以是 "all" 表示查询所有年份）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 伤因分布数据
     */
    @GetMapping("/injury-cause-distribution")
    public Result getInjuryCauseDistributionData(@RequestParam(required = false) String year,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate,
                                                @RequestParam(required = false) String timePeriod,
                                                @RequestParam(required = false) String customStartTime,
                                                @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数：将 "all" 或 "NaN" 转换为 null
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty() && !year.equalsIgnoreCase("NaN")) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            // 处理时间段参数
            Integer timePeriodInt = null;
            if (timePeriod != null && !timePeriod.equals("all") && !timePeriod.isEmpty()) {
                try {
                    timePeriodInt = Integer.parseInt(timePeriod);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            List<Map<String, Object>> distributionData = patientStatisticsService.getInjuryCauseDistributionData(yearInt, startDate, endDate, null, timePeriodInt, customStartTime, customEndTime);
            return Result.ok(distributionData);
        } catch (Exception e) {
            return Result.fail("获取伤因分布数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取ISS分布数据（轻伤、重伤、严重伤）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选，可以是 "all" 表示查询所有年份）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return ISS分布数据
     */
    @GetMapping("/iss-distribution")
    public Result getISSDistributionData(@RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate,
                                        @RequestParam(required = false) String year,
                                        @RequestParam(required = false) String timePeriod,
                                        @RequestParam(required = false) String customStartTime,
                                        @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数：将 "all" 或 "NaN" 转换为 null
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty() && !year.equalsIgnoreCase("NaN")) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            // 处理时间段参数
            Integer timePeriodInt = null;
            if (timePeriod != null && !timePeriod.equals("all") && !timePeriod.isEmpty()) {
                try {
                    timePeriodInt = Integer.parseInt(timePeriod);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            List<Map<String, Object>> distributionData = patientStatisticsService.getISSDistributionData(startDate, endDate, yearInt, null, timePeriodInt, customStartTime, customEndTime);
            return Result.ok(distributionData);
        } catch (Exception e) {
            return Result.fail("获取ISS分布数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取GCS分布数据（意识清楚、轻度意识障碍、中度意识障碍、昏迷）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选，可以是 "all" 表示查询所有年份）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return GCS分布数据
     */
    @GetMapping("/gcs-distribution")
    public Result getGCSDistributionData(@RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate,
                                        @RequestParam(required = false) String year,
                                        @RequestParam(required = false) Integer timePeriod,
                                        @RequestParam(required = false) String customStartTime,
                                        @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数：将 "all" 或 "NaN" 转换为 null
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty() && !year.equalsIgnoreCase("NaN")) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            List<Map<String, Object>> distributionData = patientStatisticsService.getGCSDistributionData(startDate, endDate, yearInt, null, timePeriod, customStartTime, customEndTime);
            return Result.ok(distributionData);
        } catch (Exception e) {
            return Result.fail("获取GCS分布数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取RTS分布数据（0-4分）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选，可以是 "all" 表示查询所有年份）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return RTS分布数据
     */
    @GetMapping("/rts-distribution")
    public Result getRTSDistributionData(@RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate,
                                        @RequestParam(required = false) String year,
                                        @RequestParam(required = false) Integer timePeriod,
                                        @RequestParam(required = false) String customStartTime,
                                        @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数：将 "all" 或 "NaN" 转换为 null
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty() && !year.equalsIgnoreCase("NaN")) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            List<Map<String, Object>> distributionData = patientStatisticsService.getRTSDistributionData(startDate, endDate, yearInt, null, timePeriod, customStartTime, customEndTime);
            return Result.ok(distributionData);
        } catch (Exception e) {
            return Result.fail("获取RTS分布数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取人群身体热力图数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选，可以是 "all" 表示查询所有年份）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param ageGroup 年龄组（可选，0-儿童，1-青年，2-中年，3-老年）
     * @param gender 性别（可选，0-男，1-女）
     * @param severity 严重程度（可选，0-轻伤，1-重伤，2-严重伤）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 人群身体热力图数据
     */
    @GetMapping("/population-body-heatmap")
    public Result getPopulationBodyHeatmapData(@RequestParam(required = false) String startDate,
                                             @RequestParam(required = false) String endDate,
                                             @RequestParam(required = false) String year,
                                             @RequestParam(required = false) Integer timePeriod,
                                             @RequestParam(required = false) Integer ageGroup,
                                             @RequestParam(required = false) Integer gender,
                                             @RequestParam(required = false) Integer severity,
                                             @RequestParam(required = false) String customStartTime,
                                             @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数：将 "all" 或 "NaN" 转换为 null
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty() && !year.equalsIgnoreCase("NaN")) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            List<Map<String, Object>> heatmapData = patientStatisticsService.getPopulationBodyHeatmapData(startDate, endDate, yearInt, null, timePeriod, ageGroup, gender, severity, customStartTime, customEndTime);
            return Result.ok(heatmapData);
        } catch (Exception e) {
            return Result.fail("获取人群身体热力图数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取死亡患者ID列表
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param year 年份（可选）
     * @param timePeriod 时间段（可选，night-夜间，morning_peak-早高峰，noon_peak-午高峰，afternoon-下午，evening_peak-晚高峰，evening-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 死亡患者ID列表
     */
    @GetMapping("/death-patient-ids")
    public Result getDeathPatientIds(@RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    @RequestParam(required = false) String year,
                                    @RequestParam(required = false) String timePeriod,
                                    @RequestParam(required = false) String customStartTime,
                                    @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty()) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            // 处理时间段参数
            Integer timePeriodInt = null;
            if (timePeriod != null && !timePeriod.equals("all") && !timePeriod.isEmpty()) {
                switch (timePeriod) {
                    case "night": timePeriodInt = 0; break;
                    case "morning_peak": timePeriodInt = 1; break;
                    case "noon_peak": timePeriodInt = 2; break;
                    case "afternoon": timePeriodInt = 3; break;
                    case "evening_peak": timePeriodInt = 4; break;
                    case "evening": timePeriodInt = 5; break;
                }
            }
            
            List<Integer> patientIds = patientStatisticsService.getDeathPatientIds(startDate, endDate, yearInt, null, timePeriodInt, customStartTime, customEndTime);
            return Result.ok(patientIds);
        } catch (Exception e) {
            return Result.fail("获取死亡患者ID列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据伤因类型获取患者ID列表
     * @param injuryCauseCategory 伤因类型（0-交通伤，1-高坠伤，2-机械伤，3-跌倒，4-其他）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param year 年份（可选）
     * @param timePeriod 时间段（可选，night-夜间，morning_peak-早高峰，noon_peak-午高峰，afternoon-下午，evening_peak-晚高峰，evening-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 患者ID列表
     */
    @GetMapping("/injury-cause-patient-ids")
    public Result getInjuryCausePatientIds(@RequestParam Integer injuryCauseCategory,
                                          @RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate,
                                          @RequestParam(required = false) String year,
                                          @RequestParam(required = false) String timePeriod,
                                          @RequestParam(required = false) String customStartTime,
                                          @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty()) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            // 处理时间段参数
            Integer timePeriodInt = null;
            if (timePeriod != null && !timePeriod.equals("all") && !timePeriod.isEmpty()) {
                switch (timePeriod) {
                    case "night": timePeriodInt = 0; break;
                    case "morning_peak": timePeriodInt = 1; break;
                    case "noon_peak": timePeriodInt = 2; break;
                    case "afternoon": timePeriodInt = 3; break;
                    case "evening_peak": timePeriodInt = 4; break;
                    case "evening": timePeriodInt = 5; break;
                }
            }
            
            List<Integer> patientIds = patientStatisticsService.getInjuryCausePatientIds(injuryCauseCategory, startDate, endDate, yearInt, null, timePeriodInt, customStartTime, customEndTime);
            return Result.ok(patientIds);
        } catch (Exception e) {
            return Result.fail("获取患者ID列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ISS分段获取患者ID列表
     * @param issSegment ISS分段类型（light-轻度损伤ISS≤16，severe-重度损伤16<ISS≤25，critical-危害损伤ISS>25）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param year 年份（可选）
     * @param timePeriod 时间段（可选，night-夜间，morning_peak-早高峰，noon_peak-午高峰，afternoon-下午，evening_peak-晚高峰，evening-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 患者ID列表
     */
    @GetMapping("/iss-segment-patient-ids")
    public Result getISSSegmentPatientIds(@RequestParam String issSegment,
                                          @RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate,
                                          @RequestParam(required = false) String year,
                                          @RequestParam(required = false) String timePeriod,
                                          @RequestParam(required = false) String customStartTime,
                                          @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty()) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            // 处理时间段参数（与 getISSDistributionData 保持一致）
            Integer timePeriodInt = null;
            if (timePeriod != null && !timePeriod.equals("all") && !timePeriod.isEmpty()) {
                try {
                    // 尝试直接解析为整数（前端传递的是数字 0-5）
                    timePeriodInt = Integer.parseInt(timePeriod);
                } catch (NumberFormatException e) {
                    // 如果不是数字，则按字符串匹配（兼容性处理）
                    switch (timePeriod) {
                        case "night": timePeriodInt = 0; break;
                        case "morning_peak": timePeriodInt = 1; break;
                        case "noon_peak": timePeriodInt = 2; break;
                        case "afternoon": timePeriodInt = 3; break;
                        case "evening_peak": timePeriodInt = 4; break;
                        case "evening": timePeriodInt = 5; break;
                    }
                }
            }
            
            List<Integer> patientIds = patientStatisticsService.getISSSegmentPatientIds(issSegment, startDate, endDate, yearInt, null, timePeriodInt, customStartTime, customEndTime);
            return Result.ok(patientIds);
        } catch (Exception e) {
            return Result.fail("获取患者ID列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据GCS分段获取患者ID列表
     * @param gcsSegment GCS分段类型（clear-意识清楚15分，mild-轻度意识障碍12-14分，moderate-中度意识障碍9-11分，coma-昏迷3-8分）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param year 年份（可选）
     * @param timePeriod 时间段（可选，night-夜间，morning_peak-早高峰，noon_peak-午高峰，afternoon-下午，evening_peak-晚高峰，evening-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 患者ID列表
     */
    @GetMapping("/gcs-segment-patient-ids")
    public Result getGCSSegmentPatientIds(@RequestParam String gcsSegment,
                                          @RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate,
                                          @RequestParam(required = false) String year,
                                          @RequestParam(required = false) String timePeriod,
                                          @RequestParam(required = false) String customStartTime,
                                          @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty()) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            // 处理时间段参数（与 getGCSDistributionData 保持一致）
            Integer timePeriodInt = null;
            if (timePeriod != null && !timePeriod.equals("all") && !timePeriod.isEmpty()) {
                try {
                    // 尝试直接解析为整数（前端传递的是数字 0-5）
                    timePeriodInt = Integer.parseInt(timePeriod);
                } catch (NumberFormatException e) {
                    // 如果不是数字，则按字符串匹配（兼容性处理）
                    switch (timePeriod) {
                        case "night": timePeriodInt = 0; break;
                        case "morning_peak": timePeriodInt = 1; break;
                        case "noon_peak": timePeriodInt = 2; break;
                        case "afternoon": timePeriodInt = 3; break;
                        case "evening_peak": timePeriodInt = 4; break;
                        case "evening": timePeriodInt = 5; break;
                    }
                }
            }
            
            List<Integer> patientIds = patientStatisticsService.getGCSSegmentPatientIds(gcsSegment, startDate, endDate, yearInt, null, timePeriodInt, customStartTime, customEndTime);
            return Result.ok(patientIds);
        } catch (Exception e) {
            return Result.fail("获取患者ID列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/rts-score-patient-ids")
    public Result getRTSScorePatientIds(@RequestParam Integer rtsScore,
                                        @RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate,
                                        @RequestParam(required = false) String year,
                                        @RequestParam(required = false) String timePeriod,
                                        @RequestParam(required = false) String customStartTime,
                                        @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty()) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            // 处理时间段参数（与 getRTSDistributionData 保持一致）
            Integer timePeriodInt = null;
            if (timePeriod != null && !timePeriod.equals("all") && !timePeriod.isEmpty()) {
                try {
                    // 尝试直接解析为整数（前端传递的是数字 0-5）
                    timePeriodInt = Integer.parseInt(timePeriod);
                } catch (NumberFormatException e) {
                    // 如果不是数字，则按字符串匹配（兼容性处理）
                    switch (timePeriod) {
                        case "night": timePeriodInt = 0; break;
                        case "morning_peak": timePeriodInt = 1; break;
                        case "noon_peak": timePeriodInt = 2; break;
                        case "afternoon": timePeriodInt = 3; break;
                        case "evening_peak": timePeriodInt = 4; break;
                        case "evening": timePeriodInt = 5; break;
                    }
                }
            }
            
            List<Integer> patientIds = patientStatisticsService.getRTSScorePatientIds(rtsScore, startDate, endDate, yearInt, null, timePeriodInt, customStartTime, customEndTime);
            return Result.ok(patientIds);
        } catch (Exception e) {
            return Result.fail("获取患者ID列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据身体部位获取患者ID列表
     * @param bodyPart 身体部位（head_neck-头颈部，face-面部，chest-胸部，abdomen-腹部，limbs-四肢，body-体表）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param year 年份（可选）
     * @param timePeriod 时间段（可选，night-夜间，morning_peak-早高峰，noon_peak-午高峰，afternoon-下午，evening_peak-晚高峰，evening-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 患者ID列表
     */
    @GetMapping("/body-part-patient-ids")
    public Result getBodyPartPatientIds(@RequestParam String bodyPart,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate,
                                       @RequestParam(required = false) String year,
                                       @RequestParam(required = false) String timePeriod,
                                       @RequestParam(required = false) String customStartTime,
                                       @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty()) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            // 处理时间段参数
            Integer timePeriodInt = null;
            if (timePeriod != null && !timePeriod.equals("all") && !timePeriod.isEmpty()) {
                try {
                    // 尝试直接解析为整数（前端传递的是数字 0-5）
                    timePeriodInt = Integer.parseInt(timePeriod);
                } catch (NumberFormatException e) {
                    // 如果不是数字，则按字符串匹配（兼容性处理）
                    switch (timePeriod) {
                        case "night": timePeriodInt = 0; break;
                        case "morning_peak": timePeriodInt = 1; break;
                        case "noon_peak": timePeriodInt = 2; break;
                        case "afternoon": timePeriodInt = 3; break;
                        case "evening_peak": timePeriodInt = 4; break;
                        case "evening": timePeriodInt = 5; break;
                    }
                }
            }
            
            List<Integer> patientIds = patientStatisticsService.getBodyPartPatientIds(bodyPart, startDate, endDate, yearInt, null, timePeriodInt, customStartTime, customEndTime);
            return Result.ok(patientIds);
        } catch (Exception e) {
            return Result.fail("获取患者ID列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取身体区域+严重程度患者ID列表
     * @param bodyRegion 身体区域（head_neck-头颈部，face-面部，chest-胸部，abdomen-腹部，limbs-四肢，body-体表）
     * @param severityLevel 严重程度（mild-轻度，moderate-中度，severe-重度，critical-无法医治）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param year 年份（可选，可以是 "all" 表示查询所有年份）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param customStartTime 自定义开始时间（可选，格式：HH:mm）
     * @param customEndTime 自定义结束时间（可选，格式：HH:mm）
     * @return 患者ID列表
     */
    @GetMapping("/body-region-severity-patient-ids")
    public Result getBodyRegionSeverityPatientIds(@RequestParam(required = false) String bodyRegion,
                                                  @RequestParam(required = false) String severityLevel,
                                                  @RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate,
                                                  @RequestParam(required = false) String year,
                                                  @RequestParam(required = false) String timePeriod,
                                                  @RequestParam(required = false) String customStartTime,
                                                  @RequestParam(required = false) String customEndTime) {
        try {
            // 处理年份参数
            Integer yearInt = null;
            if (year != null && !year.equals("all") && !year.isEmpty()) {
                try {
                    yearInt = Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    // 如果转换失败，保持为null
                }
            }
            
            // 处理时间段参数
            Integer timePeriodInt = null;
            if (timePeriod != null && !timePeriod.equals("all") && !timePeriod.isEmpty()) {
                try {
                    // 尝试直接解析为整数（前端传递的是数字 0-5）
                    timePeriodInt = Integer.parseInt(timePeriod);
                } catch (NumberFormatException e) {
                    // 如果不是数字，则按字符串匹配（兼容性处理）
                    switch (timePeriod) {
                        case "night": timePeriodInt = 0; break;
                        case "morning_peak": timePeriodInt = 1; break;
                        case "noon_peak": timePeriodInt = 2; break;
                        case "afternoon": timePeriodInt = 3; break;
                        case "evening_peak": timePeriodInt = 4; break;
                        case "evening": timePeriodInt = 5; break;
                    }
                }
            }
            
            List<Integer> patientIds = patientStatisticsService.getBodyRegionSeverityPatientIds(bodyRegion, severityLevel, startDate, endDate, yearInt, null, timePeriodInt, customStartTime, customEndTime);
            return Result.ok(patientIds);
        } catch (Exception e) {
            return Result.fail("获取患者ID列表失败：" + e.getMessage());
        }
    }
}
