package com.demo.Service.impl.impl;

import com.demo.Service.impl.IPatientStatisticsService;
import com.demo.dto.PatientStatisticsDTO;
import com.demo.mapper.PatientStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 患者统计服务实现类
 */
@Service
public class PatientStatisticsServiceImpl implements IPatientStatisticsService {
    
    @Autowired
    private PatientStatisticsMapper patientStatisticsMapper;
    
    @Override
    public PatientStatisticsDTO getPatientStatistics(String startDate, String endDate) {
        // 如果没有指定日期范围，使用默认范围（最近一年）
        if (startDate == null || endDate == null) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusYears(1);
            startDate = start.format(DateTimeFormatter.ISO_LOCAL_DATE);
            endDate = end.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        // 计算总天数
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        
        // 获取统计数据
        Long totalPatients = patientStatisticsMapper.getTotalPatients(startDate, endDate);
        Double averageInterventionTime = patientStatisticsMapper.getAverageInterventionTime(startDate, endDate);
        Double successRate = patientStatisticsMapper.getSuccessRate(startDate, endDate);
        
        // 计算日均患者数
        Double averagePatientsPerDay = totalPatients != null && totalDays > 0 ? 
            totalPatients.doubleValue() / totalDays : 0.0;
        
        return new PatientStatisticsDTO(
            totalPatients != null ? totalPatients : 0L,
            averagePatientsPerDay,
            averageInterventionTime != null ? averageInterventionTime : 0.0,
            successRate != null ? successRate : 0.0,
            startDate,
            endDate,
            (int) totalDays
        );
    }
    
    @Override
    public List<Map<String, Object>> getMonthlyTimeHeatmapData(Integer year, String startDate, String endDate, Integer season, Integer timePeriod) {
        // 如果没有指定年份，使用当前年份
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        List<Map<String, Object>> rawData = patientStatisticsMapper.getMonthlyTimeHeatmapData(year, startDate, endDate, season, timePeriod);
        
        // 处理7x13矩阵数据
        return processHeatmapData(rawData);
    }
    
    /**
     * 处理热力图数据，构建7x13矩阵
     * @param rawData 原始数据
     * @return 处理后的矩阵数据
     */
    private List<Map<String, Object>> processHeatmapData(List<Map<String, Object>> rawData) {
        // 定义时间段顺序
        String[] timePeriods = {
            "night_0_7", "morning_rush_8_9", "lunch_rush_10_11", 
            "afternoon_12_16", "evening_rush_17_19", "night_20_23", "total"
        };
        
        // 初始化7x13矩阵
        int[][] matrix = new int[7][13];
        
        // 填充矩阵数据
        for (Map<String, Object> record : rawData) {
            String timePeriod = (String) record.get("time_period");
            Integer month = (Integer) record.get("month");
            Long patientCount = ((Number) record.get("patient_count")).longValue();
            
            // 找到时间段索引
            int timeIndex = -1;
            for (int i = 0; i < timePeriods.length; i++) {
                if (timePeriods[i].equals(timePeriod)) {
                    timeIndex = i;
                    break;
                }
            }
            
            // 处理月份索引（1-12月对应索引0-11，13表示总和列对应索引12）
            int monthIndex = -1;
            if (month >= 1 && month <= 12) {
                monthIndex = month - 1; // 1月对应索引0，12月对应索引11
            } else if (month == 13) {
                monthIndex = 12; // 总和列对应索引12
            }
            
            // 设置矩阵值
            if (timeIndex >= 0 && monthIndex >= 0) {
                matrix[timeIndex][monthIndex] = patientCount.intValue();
            }
        }
        
        // 计算总和行和总和列
        calculateTotals(matrix);
        
        // 转换为返回格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("time_period", timePeriods[i]);
            row.put("data", matrix[i]);
            result.add(row);
        }
        
        return result;
    }
    
    /**
     * 计算总和行和总和列
     * @param matrix 矩阵数据
     */
    private void calculateTotals(int[][] matrix) {
        // 计算每行的总和（时间段总和）
        for (int i = 0; i < 6; i++) { // 前6行是时间段，第7行是总和行
            int rowSum = 0;
            for (int j = 0; j < 12; j++) { // 前12列是月份，第13列是总和列
                rowSum += matrix[i][j];
            }
            matrix[i][12] = rowSum; // 设置总和列
        }
        
        // 计算每列的总和（月份总和）
        for (int j = 0; j < 13; j++) { // 包括总和列
            int colSum = 0;
            for (int i = 0; i < 6; i++) { // 前6行是时间段
                colSum += matrix[i][j];
            }
            matrix[6][j] = colSum; // 设置总和行
        }
    }
    
    @Override
    public List<Map<String, Object>> getInjuryAnalysisData(String startDate, String endDate, Integer season, Integer timePeriod) {
        // 如果没有指定日期范围，使用默认范围（最近一年）
        if (startDate == null || endDate == null) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusYears(1);
            startDate = start.format(DateTimeFormatter.ISO_LOCAL_DATE);
            endDate = end.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        return patientStatisticsMapper.getInjuryAnalysisData(startDate, endDate, season, timePeriod);
    }
    
    @Override
    public List<Map<String, Object>> getISSScoreDistributionData(String startDate, String endDate) {
        // 如果没有指定日期范围，使用默认范围（最近一年）
        if (startDate == null || endDate == null) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusYears(1);
            startDate = start.format(DateTimeFormatter.ISO_LOCAL_DATE);
            endDate = end.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        return patientStatisticsMapper.getISSScoreDistributionData(startDate, endDate);
    }
    
    @Override
    public List<Map<String, Object>> getBodyRegionInjuryData(String startDate, String endDate) {
        // 如果没有指定日期范围，使用默认范围（最近一年）
        if (startDate == null || endDate == null) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusYears(1);
            startDate = start.format(DateTimeFormatter.ISO_LOCAL_DATE);
            endDate = end.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        return patientStatisticsMapper.getBodyRegionInjuryData(startDate, endDate);
    }
    
    @Override
    public List<Map<String, Object>> getInterventionTimeEfficiencyData(String startDate, String endDate) {
        // 如果没有指定日期范围，使用默认范围（最近一年）
        if (startDate == null || endDate == null) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusYears(1);
            startDate = start.format(DateTimeFormatter.ISO_LOCAL_DATE);
            endDate = end.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        return patientStatisticsMapper.getInterventionTimeEfficiencyData(startDate, endDate);
    }
    
    @Override
    public List<Map<String, Object>> getPatientFlowData(String startDate, String endDate) {
        // 如果没有指定日期范围，使用默认范围（最近一年）
        if (startDate == null || endDate == null) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusYears(1);
            startDate = start.format(DateTimeFormatter.ISO_LOCAL_DATE);
            endDate = end.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        return patientStatisticsMapper.getPatientFlowData(startDate, endDate);
    }
    
    @Override
    public List<Map<String, Object>> getInjuryCauseDistributionData(Integer year, String startDate, String endDate, Integer season, Integer timePeriod) {
        // 如果没有指定年份，使用当前年份
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        // 如果没有指定日期范围，使用指定年份的全年范围
        if (startDate == null || endDate == null) {
            startDate = year + "-01-01";
            endDate = year + "-12-31";
        }
        
        List<Map<String, Object>> rawData = patientStatisticsMapper.getInjuryCauseDistributionData(year, startDate, endDate, season, timePeriod);
        
        // 处理伤因分布数据，构建12个月x5种伤因的柱状图数据
        return processInjuryCauseData(rawData);
    }
    
    /**
     * 处理伤因分布数据，构建柱状图数据
     * @param rawData 原始数据
     * @return 处理后的柱状图数据
     */
    private List<Map<String, Object>> processInjuryCauseData(List<Map<String, Object>> rawData) {
        // 定义伤因分类映射
        Map<Integer, String> causeMapping = new HashMap<>();
        causeMapping.put(0, "交通伤");
        causeMapping.put(1, "高坠伤");
        causeMapping.put(2, "机械伤");
        causeMapping.put(3, "跌倒");
        causeMapping.put(4, "其他");
        
        
        // 初始化12个月x5种伤因的矩阵
        int[][] matrix = new int[12][5];
        
        // 填充矩阵数据
        for (Map<String, Object> record : rawData) {
            Integer month = (Integer) record.get("month");
            Integer causeCategory = (Integer) record.get("injury_cause_category");
            Long patientCount = ((Number) record.get("patient_count")).longValue();
            
            if (month != null && causeCategory != null && month >= 1 && month <= 12 && causeCategory >= 0 && causeCategory <= 4) {
                matrix[month - 1][causeCategory] = patientCount.intValue();
            }
        }
        
        // 构建返回数据 - 转换为List<Map<String, Object>>格式
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 为每个伤因分类创建数据项
        for (int cause = 0; cause < 5; cause++) {
            Map<String, Object> causeData = new HashMap<>();
            causeData.put("cause_name", causeMapping.get(cause));
            causeData.put("cause_category", cause);
            
            // 添加12个月的数据
            List<Integer> monthlyData = new ArrayList<>();
            for (int month = 0; month < 12; month++) {
                monthlyData.add(matrix[month][cause]);
            }
            causeData.put("monthly_data", monthlyData);
            
            // 计算总数
            int total = monthlyData.stream().mapToInt(Integer::intValue).sum();
            causeData.put("total_count", total);
            
            // 设置颜色
            String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7"};
            causeData.put("color", colors[cause]);
            
            result.add(causeData);
        }
        
        return result;
    }
}
