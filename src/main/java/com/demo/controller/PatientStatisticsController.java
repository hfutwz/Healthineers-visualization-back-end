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
     * @return 患者统计数据
     */
    @GetMapping("/statistics")
    public Result getPatientStatistics(@RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate) {
        try {
            PatientStatisticsDTO statistics = patientStatisticsService.getPatientStatistics(startDate, endDate);
            return Result.ok(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取患者统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取月度时间热力图数据
     * @param year 年份（可选）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return 热力图数据
     */
    @GetMapping("/monthly-heatmap")
    public Result getMonthlyTimeHeatmapData(@RequestParam(required = false) Integer year,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate,
                                           @RequestParam(required = false) Integer season,
                                           @RequestParam(required = false) Integer timePeriod) {
        try {
            Object heatmapData = patientStatisticsService.getMonthlyTimeHeatmapData(year, startDate, endDate, season, timePeriod);
            return Result.ok(heatmapData);
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
            return Result.fail("获取身体区域损伤数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取身体区域损伤旭日图数据
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 身体区域损伤旭日图数据
     */
    @GetMapping("/body-region-sunburst")
    public Result getBodyRegionSunburstData(@RequestParam(required = false) Integer season,
                                          @RequestParam(required = false) Integer timePeriod,
                                          @RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate) {
        try {
            List<Map<String, Object>> sunburstData = patientStatisticsService.getBodyRegionSunburstData(season, timePeriod, startDate, endDate);
            return Result.ok(sunburstData);
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
            return Result.fail("获取患者流向数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取伤因分布数据
     * @param year 年份（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return 伤因分布数据
     */
    @GetMapping("/injury-cause-distribution")
    public Result getInjuryCauseDistributionData(@RequestParam(required = false) Integer year,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate,
                                                @RequestParam(required = false) String season,
                                                @RequestParam(required = false) String timePeriod) {
        try {
            // 处理季节参数
            Integer seasonInt = null;
            if (season != null && !season.equals("all") && !season.isEmpty()) {
                try {
                    seasonInt = Integer.parseInt(season);
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
            
            List<Map<String, Object>> distributionData = patientStatisticsService.getInjuryCauseDistributionData(year, startDate, endDate, seasonInt, timePeriodInt);
            return Result.ok(distributionData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取伤因分布数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取ISS分布数据（轻伤、重伤、严重伤）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return ISS分布数据
     */
    @GetMapping("/iss-distribution")
    public Result getISSDistributionData(@RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate,
                                        @RequestParam(required = false) Integer year,
                                        @RequestParam(required = false) Integer season,
                                        @RequestParam(required = false) Integer timePeriod) {
        try {
            List<Map<String, Object>> distributionData = patientStatisticsService.getISSDistributionData(startDate, endDate, year, season, timePeriod);
            return Result.ok(distributionData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取ISS分布数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取GCS分布数据（意识清楚、轻度意识障碍、中度意识障碍、昏迷）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return GCS分布数据
     */
    @GetMapping("/gcs-distribution")
    public Result getGCSDistributionData(@RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate,
                                        @RequestParam(required = false) Integer year,
                                        @RequestParam(required = false) Integer season,
                                        @RequestParam(required = false) Integer timePeriod) {
        try {
            List<Map<String, Object>> distributionData = patientStatisticsService.getGCSDistributionData(startDate, endDate, year, season, timePeriod);
            return Result.ok(distributionData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取GCS分布数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取RTS分布数据（0-4分）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param year 年份（可选）
     * @param season 季节（可选，0-春季，1-夏季，2-秋季，3-冬季）
     * @param timePeriod 时间段（可选，0-夜间，1-早高峰，2-午高峰，3-下午，4-晚高峰，5-晚上）
     * @return RTS分布数据
     */
    @GetMapping("/rts-distribution")
    public Result getRTSDistributionData(@RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate,
                                        @RequestParam(required = false) Integer year,
                                        @RequestParam(required = false) Integer season,
                                        @RequestParam(required = false) Integer timePeriod) {
        try {
            List<Map<String, Object>> distributionData = patientStatisticsService.getRTSDistributionData(startDate, endDate, year, season, timePeriod);
            return Result.ok(distributionData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("获取RTS分布数据失败：" + e.getMessage());
        }
    }
}
