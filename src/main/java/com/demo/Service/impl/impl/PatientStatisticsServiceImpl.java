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
        
        // 如果没有指定日期范围，使用指定年份的全年范围
        if (startDate == null || endDate == null) {
            startDate = year + "-01-01";
            endDate = year + "-12-31";
        }
        
        System.out.println("查询参数 - 年份: " + year + ", 开始日期: " + startDate + ", 结束日期: " + endDate + 
                          ", 季节: " + season + ", 时间段: " + timePeriod);
        
        List<Map<String, Object>> rawData = patientStatisticsMapper.getMonthlyTimeHeatmapData(year, startDate, endDate, season, timePeriod);
        
        System.out.println("查询结果数量: " + (rawData != null ? rawData.size() : 0));
        if (rawData != null && !rawData.isEmpty()) {
            System.out.println("第一条数据: " + rawData.get(0));
        }
        
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
        
        // 定义时间段中文名称
        String[] timePeriodNames = {
            "夜间(0-7时)", "早高峰(8-9时)", "午高峰(10-11时)", 
            "下午(12-16时)", "晚高峰(17-19时)", "晚上(20-23时)", "总计"
        };
        
        // 初始化7x13矩阵
        int[][] matrix = new int[7][13];
        
        System.out.println("开始处理热力图数据，原始数据条数: " + (rawData != null ? rawData.size() : 0));
        
        // 填充矩阵数据
        if (rawData != null) {
            for (Map<String, Object> record : rawData) {
                String timePeriod = (String) record.get("time_period");
                Integer month = (Integer) record.get("month");
                Long patientCount = ((Number) record.get("patient_count")).longValue();
                
                System.out.println("处理记录: timePeriod=" + timePeriod + ", month=" + month + ", count=" + patientCount);
                
                // 找到时间段索引
                int timeIndex = -1;
                for (int i = 0; i < timePeriods.length; i++) {
                    if (timePeriods[i].equals(timePeriod)) {
                        timeIndex = i;
                        break;
                    }
                }
                
                // 处理月份索引（1-12月对应索引0-11）
                int monthIndex = -1;
                if (month != null && month >= 1 && month <= 12) {
                    monthIndex = month - 1; // 1月对应索引0，12月对应索引11
                }
                
                // 设置矩阵值
                if (timeIndex >= 0 && monthIndex >= 0) {
                    matrix[timeIndex][monthIndex] = patientCount.intValue();
                    System.out.println("设置矩阵[" + timeIndex + "][" + monthIndex + "] = " + patientCount.intValue());
                }
            }
        }
        
        // 计算总和行和总和列
        calculateTotals(matrix);
        
        // 转换为返回格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("time_period", timePeriods[i]);
            row.put("time_period_name", timePeriodNames[i]);
            row.put("data", matrix[i]);
            result.add(row);
        }
        
        System.out.println("热力图数据处理完成，返回结果条数: " + result.size());
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
    public List<Map<String, Object>> getBodyRegionSunburstData(Integer season, Integer timePeriod, String startDate, String endDate) {
        // 如果没有指定日期范围，使用默认范围（最近一年）
        if (startDate == null || endDate == null) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusYears(1);
            startDate = start.format(DateTimeFormatter.ISO_LOCAL_DATE);
            endDate = end.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        return patientStatisticsMapper.getBodyRegionSunburstData(season, timePeriod, startDate, endDate);
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
    
    @Override
    public List<Map<String, Object>> getISSDistributionData(String startDate, String endDate, Integer year, Integer season, Integer timePeriod) {
        // 如果没有指定日期范围，使用默认范围（最近一年）
        if (startDate == null || endDate == null) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusYears(1);
            startDate = start.format(DateTimeFormatter.ISO_LOCAL_DATE);
            endDate = end.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        List<Map<String, Object>> rawData = patientStatisticsMapper.getISSDistributionData(startDate, endDate, year, season, timePeriod);
        
        // 处理ISS分布数据，转换为饼状图格式
        return processISSDistributionData(rawData);
    }
    
    /**
     * 处理ISS分布数据，转换为饼状图格式
     * @param rawData 原始数据
     * @return 处理后的饼状图数据
     */
    private List<Map<String, Object>> processISSDistributionData(List<Map<String, Object>> rawData) {
        // 定义伤情等级映射
        Map<String, String> severityMapping = new HashMap<>();
        severityMapping.put("light", "轻伤(ISS≤16)");
        severityMapping.put("severe", "重伤(16<ISS≤25)");
        severityMapping.put("critical", "严重伤(ISS>25)");
        
        // 定义颜色映射
        Map<String, String> colorMapping = new HashMap<>();
        colorMapping.put("light", "#52C41A");      // 绿色 - 轻伤
        colorMapping.put("severe", "#FA8C16");     // 橙色 - 重伤
        colorMapping.put("critical", "#F5222D");   // 红色 - 严重伤
        
        // 初始化所有等级的数据计数
        Map<String, Long> severityCounts = new HashMap<>();
        severityCounts.put("light", 0L);
        severityCounts.put("severe", 0L);
        severityCounts.put("critical", 0L);
        
        // 处理原始数据，统计各等级数量
        for (Map<String, Object> record : rawData) {
            String severityLevel = (String) record.get("severity_level");
            Long patientCount = ((Number) record.get("patient_count")).longValue();
            
            // 确保severityLevel不为null且有效
            if (severityLevel != null && severityCounts.containsKey(severityLevel)) {
                severityCounts.put(severityLevel, patientCount);
            }
        }
        
        // 计算总患者数
        long totalPatients = severityCounts.values().stream().mapToLong(Long::longValue).sum();
        
        // 构建饼状图数据 - 确保返回所有三个等级
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 按顺序添加三个等级的数据
        String[] severityLevels = {"light", "severe", "critical"};
        for (String level : severityLevels) {
            Long patientCount = severityCounts.get(level);
            double percentage = totalPatients > 0 ? 
                Math.round(patientCount * 100.0 / totalPatients * 100) / 100.0 : 0.0;
            
            Map<String, Object> pieData = new HashMap<>();
            // 直接使用硬编码的中文字符串，避免编码问题
            String name = "";
            try {
                if ("light".equals(level)) {
                    name = "轻伤(ISS≤16)";
                } else if ("severe".equals(level)) {
                    name = "重伤(16<ISS≤25)";
                } else if ("critical".equals(level)) {
                    name = "严重伤(ISS>25)";
                }
            } catch (Exception e) {
                // 如果编码有问题，使用英文标签
                if ("light".equals(level)) {
                    name = "Light Injury (ISS≤16)";
                } else if ("severe".equals(level)) {
                    name = "Severe Injury (16<ISS≤25)";
                } else if ("critical".equals(level)) {
                    name = "Critical Injury (ISS>25)";
                }
            }
            pieData.put("name", name);
            pieData.put("value", patientCount);
            pieData.put("color", colorMapping.get(level));
            pieData.put("percentage", percentage);
            
            result.add(pieData);
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getGCSDistributionData(String startDate, String endDate, Integer year, Integer season, Integer timePeriod) {
        // 如果没有指定日期范围，使用默认范围（最近一年）
        if (startDate == null || endDate == null) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusYears(1);
            startDate = start.format(DateTimeFormatter.ISO_LOCAL_DATE);
            endDate = end.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        // 获取GCS分布数据
        List<Map<String, Object>> rawData = patientStatisticsMapper.getGCSDistributionData(startDate, endDate, year, season, timePeriod);
        
        // 计算总数
        int totalCount = rawData.stream().mapToInt(item -> ((Number) item.get("count")).intValue()).sum();
        
        // 处理数据，添加颜色和百分比
        List<Map<String, Object>> result = new ArrayList<>();
        
        // GCS分类映射
        Map<String, String> nameMapping = new HashMap<>();
        nameMapping.put("15", "意识清楚 (15分)");
        nameMapping.put("12-14", "轻度意识障碍 (12-14分)");
        nameMapping.put("9-11", "中度意识障碍 (9-11分)");
        nameMapping.put("3-8", "昏迷 (3-8分)");
        
        // 颜色映射
        Map<String, String> colorMapping = new HashMap<>();
        colorMapping.put("15", "#52C41A");      // 绿色 - 意识清楚
        colorMapping.put("12-14", "#1890FF");    // 蓝色 - 轻度意识障碍
        colorMapping.put("9-11", "#FA8C16");     // 橙色 - 中度意识障碍
        colorMapping.put("3-8", "#F5222D");      // 红色 - 昏迷
        
        for (Map<String, Object> item : rawData) {
            String level = (String) item.get("level");
            Integer patientCount = ((Number) item.get("count")).intValue();
            double percentage = totalCount > 0 ? (double) patientCount / totalCount * 100 : 0;
            
            Map<String, Object> pieData = new HashMap<>();
            String name = nameMapping.getOrDefault(level, level);
            pieData.put("name", name);
            pieData.put("value", patientCount);
            pieData.put("color", colorMapping.getOrDefault(level, "#666666"));
            pieData.put("percentage", Math.round(percentage * 100.0) / 100.0);
            
            result.add(pieData);
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getRTSDistributionData(String startDate, String endDate, Integer year, Integer season, Integer timePeriod) {
        // 获取RTS分布数据
        List<Map<String, Object>> rawData = patientStatisticsMapper.getRTSDistributionData(startDate, endDate, year, season, timePeriod);
        
        // 计算总数
        int totalCount = rawData.stream().mapToInt(item -> ((Number) item.get("count")).intValue()).sum();
        
        // 处理数据，添加颜色和百分比
        List<Map<String, Object>> result = new ArrayList<>();
        
        // RTS分类映射 (总分0-12分)
        Map<String, String> nameMapping = new HashMap<>();
        nameMapping.put("12", "RTS评分12分");
        nameMapping.put("11", "RTS评分11分");
        nameMapping.put("10", "RTS评分10分");
        nameMapping.put("9", "RTS评分9分");
        nameMapping.put("8", "RTS评分8分");
        nameMapping.put("7", "RTS评分7分");
        nameMapping.put("6", "RTS评分6分");
        nameMapping.put("5", "RTS评分5分");
        nameMapping.put("4", "RTS评分4分");
        nameMapping.put("3", "RTS评分3分");
        nameMapping.put("2", "RTS评分2分");
        nameMapping.put("1", "RTS评分1分");
        nameMapping.put("0", "RTS评分0分");
        
        // 颜色映射 (与GCS和ISS保持类似的色调)
        Map<String, String> colorMapping = new HashMap<>();
        colorMapping.put("12", "#52C41A");     // 绿色 - 12分 (最高分)
        colorMapping.put("11", "#73D13D");     // 浅绿色 - 11分
        colorMapping.put("10", "#95DE64");     // 更浅绿色 - 10分
        colorMapping.put("9", "#B7EB8F");     // 很浅绿色 - 9分
        colorMapping.put("8", "#D9F7BE");     // 极浅绿色 - 8分
        colorMapping.put("7", "#1890FF");     // 蓝色 - 7分
        colorMapping.put("6", "#40A9FF");     // 浅蓝色 - 6分
        colorMapping.put("5", "#69C0FF");     // 更浅蓝色 - 5分
        colorMapping.put("4", "#91D5FF");     // 很浅蓝色 - 4分
        colorMapping.put("3", "#BAE7FF");     // 极浅蓝色 - 3分
        colorMapping.put("2", "#FA8C16");     // 橙色 - 2分
        colorMapping.put("1", "#F5222D");     // 红色 - 1分
        colorMapping.put("0", "#722ED1");     // 紫色 - 0分 (最低分)
        
        for (Map<String, Object> item : rawData) {
            String score = (String) item.get("score");
            Integer patientCount = ((Number) item.get("count")).intValue();
            double percentage = totalCount > 0 ? (double) patientCount / totalCount * 100 : 0;
            
            Map<String, Object> pieData = new HashMap<>();
            String name = nameMapping.getOrDefault(score, "RTS评分" + score + "分");
            String color = colorMapping.getOrDefault(score, "#666666");
            
            // 调试信息
            System.out.println("RTS Score: " + score + ", Name: " + name + ", Color: " + color);
            
            pieData.put("name", name);
            pieData.put("value", patientCount);
            pieData.put("color", color);
            pieData.put("percentage", Math.round(percentage * 100.0) / 100.0);
            
            result.add(pieData);
        }
        
        return result;
    }
}
