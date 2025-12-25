package com.demo.Service.impl.impl;

import com.demo.Service.impl.IPatientStatisticsService;
import com.demo.dto.InterventionTimeDTO;
import com.demo.dto.PatientStatisticsDTO;
import com.demo.mapper.PatientStatisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 患者统计服务实现类
 */
@Slf4j
@Service
public class PatientStatisticsServiceImpl implements IPatientStatisticsService {
    
    @Autowired
    private PatientStatisticsMapper patientStatisticsMapper;
    
    @Override
    public PatientStatisticsDTO getPatientStatistics(String startDate, String endDate, Integer year, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        
        // 如果指定了年份但没有指定具体日期范围，使用该年份的1月1日到当前日期
        if ((startDate == null || endDate == null) && year != null) {
            startDate = year + "-01-01";
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        // 如果所有筛选条件都为 null（包括日期范围和年份），不设置默认日期范围，查询所有数据
        // 只有当用户明确选择了筛选条件时，才应用日期范围限制
        
        // 计算总天数
        long totalDays = 0;
        if (startDate != null && endDate != null) {
            // 如果指定了日期范围，使用指定的日期范围计算总天数
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
            totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        } else {
            // 如果查询全部数据（没有指定日期范围），获取最久远的日期，然后计算从最久远日期到当前日期的总天数
            String earliestDate = patientStatisticsMapper.getEarliestAdmissionDate(year, season, timePeriod);
            if (earliestDate != null && !earliestDate.isEmpty()) {
                LocalDate start = LocalDate.parse(earliestDate);
                LocalDate end = LocalDate.now();
                totalDays = ChronoUnit.DAYS.between(start, end) + 1;
            }
        }
        
        // 在Service层处理时间转换
        Integer customStartTimeMinutes = null;
        Integer customEndTimeMinutes = null;
        if (customStartTime != null && customEndTime != null) {
            customStartTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customStartTime);
            customEndTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customEndTime);
        }
        
        // 获取统计数据 - 现在支持自定义时间段筛选
        Long totalPatients = patientStatisticsMapper.getTotalPatients(startDate, endDate, year, season, timePeriod, customStartTimeMinutes, customEndTimeMinutes);
        
        // 获取干预时间原始数据，在Service层计算平均时间
        List<InterventionTimeDTO> interventionTimeList = patientStatisticsMapper.getAverageInterventionTime(startDate, endDate, year, season, timePeriod, customStartTimeMinutes, customEndTimeMinutes);
        
        Double averageInterventionTime = calculateAverageInterventionTime(interventionTimeList);
        
        Long deathCount = patientStatisticsMapper.getDeathCount(startDate, endDate, year, season, timePeriod, customStartTimeMinutes, customEndTimeMinutes);
        
        // 计算日均患者数
        Double averagePatientsPerDay = 0.0;
        if (totalDays > 0 && totalPatients != null) {
            averagePatientsPerDay = totalPatients.doubleValue() / totalDays;
        }
        
        return new PatientStatisticsDTO(
            totalPatients != null ? totalPatients : 0L,
            averagePatientsPerDay,
            averageInterventionTime != null ? averageInterventionTime : 0.0,
            deathCount != null ? deathCount : 0L,
            startDate,
            endDate,
            (int) totalDays
        );
    }
    
    @Override
    public List<Map<String, Object>> getMonthlyTimeHeatmapData(Integer year, String startDate, String endDate, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        
        // 不设置默认值，如果参数为null，Mapper会查询全部数据
        // 只有在用户明确指定了筛选条件时，才使用这些条件进行查询
        List<Map<String, Object>> rawData = patientStatisticsMapper.getMonthlyTimeHeatmapData(year, startDate, endDate, season, timePeriod, customStartTime, customEndTime);
        
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
        
        // 填充矩阵数据
        if (rawData != null) {
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
                
                // 处理月份索引（1-12月对应索引0-11）
                int monthIndex = -1;
                if (month != null && month >= 1 && month <= 12) {
                    monthIndex = month - 1; // 1月对应索引0，12月对应索引11
                }
                
                // 设置矩阵值
                if (timeIndex >= 0 && monthIndex >= 0) {
                    matrix[timeIndex][monthIndex] = patientCount.intValue();
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
        // 不设置默认值，只在传递了对应参数时才添加查询条件
        // startDate、endDate 如果为 null，则不在SQL中添加对应的查询条件
        
        return patientStatisticsMapper.getBodyRegionInjuryData(startDate, endDate);
    }
    
    @Override
    public List<Map<String, Object>> getBodyRegionSunburstData(Integer season, Integer timePeriod, String startDate, String endDate, Integer year, String customStartTime, String customEndTime) {
        
        // 不设置默认值，只在传递了对应参数时才添加查询条件
        // year、startDate、endDate、season、timePeriod、customStartTime、customEndTime 如果为 null，则不在SQL中添加对应的查询条件
        
        List<Map<String, Object>> rawData = patientStatisticsMapper.getBodyRegionSunburstData(season, timePeriod, startDate, endDate, year, customStartTime, customEndTime);
        
        return rawData;
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
        
        // 获取干预时间原始数据
        List<InterventionTimeDTO> interventionTimeList = patientStatisticsMapper.getInterventionTimeEfficiencyData(startDate, endDate);
        
        // 在Service层计算时间段分布
        return calculateInterventionTimeEfficiency(interventionTimeList, startDate, endDate);
    }
    
    /**
     * 计算平均干预时间（分钟）
     * @param interventionTimeList 干预时间原始数据列表
     * @return 平均干预时间（分钟），如果没有有效数据则返回null
     */
    private Double calculateAverageInterventionTime(List<InterventionTimeDTO> interventionTimeList) {
        if (interventionTimeList == null || interventionTimeList.isEmpty()) {
            return null;
        }
        
        // 过滤出完整的数据（入室和离室时间都存在），并计算时间差
        // 只计算合理的时间差（不超过48小时，即2880分钟）
        List<Long> timeDiffs = interventionTimeList.stream()
            .filter(InterventionTimeDTO::isComplete)
            .map(this::calculateTimeDifferenceMinutes)
            .filter(diff -> diff != null && diff >= 0 && diff <= 2880)  // 过滤掉无效的时间差和异常大的时间差
            .collect(Collectors.toList());
        
        if (timeDiffs.isEmpty()) {
            return null;
        }
        
        // 计算平均值
        long sum = timeDiffs.stream().mapToLong(Long::longValue).sum();
        double average = (double) sum / timeDiffs.size();
        
        return average;
    }
    
    /**
     * 计算干预时间效率数据（时间段分布）
     * @param interventionTimeList 干预时间原始数据列表
     * @param startDate 开始日期（用于计算总患者数）
     * @param endDate 结束日期（用于计算总患者数）
     * @return 时间段分布数据
     */
    private List<Map<String, Object>> calculateInterventionTimeEfficiency(
            List<InterventionTimeDTO> interventionTimeList, String startDate, String endDate) {
        
        // 获取总患者数（用于计算百分比）
        Long totalPatients = patientStatisticsMapper.getTotalPatients(startDate, endDate, null, null, null, null, null);
        if (totalPatients == null || totalPatients == 0) {
            totalPatients = 1L; // 避免除零
        }
        
        // 初始化时间段统计
        Map<String, Integer> timeRangeCounts = new HashMap<>();
        timeRangeCounts.put("无数据", 0);
        timeRangeCounts.put("30分钟以内", 0);
        timeRangeCounts.put("31-60分钟", 0);
        timeRangeCounts.put("61-120分钟", 0);
        timeRangeCounts.put("121-240分钟", 0);
        timeRangeCounts.put("240分钟以上", 0);
        
        // 计算每个患者的时间差并分类
        for (InterventionTimeDTO dto : interventionTimeList) {
            String timeRange;
            
            if (!dto.isComplete()) {
                // 数据不完整，归为"无数据"
                timeRange = "无数据";
            } else {
                Long timeDiffMinutes = calculateTimeDifferenceMinutes(dto);
                if (timeDiffMinutes == null || timeDiffMinutes < 0) {
                    timeRange = "无数据";
                } else if (timeDiffMinutes <= 30) {
                    timeRange = "30分钟以内";
                } else if (timeDiffMinutes <= 60) {
                    timeRange = "31-60分钟";
                } else if (timeDiffMinutes <= 120) {
                    timeRange = "61-120分钟";
                } else if (timeDiffMinutes <= 240) {
                    timeRange = "121-240分钟";
                } else {
                    timeRange = "240分钟以上";
                }
            }
            
            timeRangeCounts.put(timeRange, timeRangeCounts.get(timeRange) + 1);
        }
        
        // 构建返回数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : timeRangeCounts.entrySet()) {
            if (entry.getValue() > 0) {  // 只返回有数据的时段
                Map<String, Object> item = new HashMap<>();
                item.put("time_range", entry.getKey());
                item.put("patient_count", entry.getValue());
                item.put("percentage", Math.round(entry.getValue() * 100.0 / totalPatients * 100) / 100.0);
                result.add(item);
            }
        }
        
        // 按患者数量降序排序
        result.sort((a, b) -> {
            Integer countA = (Integer) a.get("patient_count");
            Integer countB = (Integer) b.get("patient_count");
            return countB.compareTo(countA);
        });
        
        return result;
    }
    
    /**
     * 计算时间差（分钟）
     * @param dto 干预时间DTO
     * @return 时间差（分钟），如果计算失败或数据异常返回null
     */
    private Long calculateTimeDifferenceMinutes(InterventionTimeDTO dto) {
        try {
            // 将4位字符串时间（如"2326"）转换为LocalDateTime
            LocalDateTime admissionDateTime = parseDateTime(dto.getAdmissionDate(), dto.getAdmissionTime());
            LocalDateTime leaveDateTime = parseDateTime(dto.getLeaveSurgeryDate(), dto.getLeaveSurgeryTime());
            
            if (admissionDateTime == null || leaveDateTime == null) {
                return null;
            }
            
            // 计算时间差（分钟）
            long minutes = ChronoUnit.MINUTES.between(admissionDateTime, leaveDateTime);
            
            // 如果时间差为负数，说明离室时间早于入室时间，数据异常
            if (minutes < 0) {
                return null;
            }
            
            // 检查日期差是否异常（超过7天认为是数据错误）
            long daysDiff = ChronoUnit.DAYS.between(admissionDateTime.toLocalDate(), leaveDateTime.toLocalDate());
            if (daysDiff > 7) {
                // 如果日期差超过7天，但时间上看起来应该是同一天或相邻天，尝试修正
                // 例如：入室2024-11-26 23:26，离室2025-10-27 00:30，可能是年份错误
                // 尝试将离室日期修正为入室日期的下一天
                LocalDate admissionDate = admissionDateTime.toLocalDate();
                
                // 如果离室时间小于入室时间（跨天），且日期差很大，尝试修正
                if (leaveDateTime.toLocalTime().isBefore(admissionDateTime.toLocalTime()) && daysDiff > 30) {
                    // 尝试修正：将离室日期设为入室日期的下一天
                    LocalDate correctedLeaveDate = admissionDate.plusDays(1);
                    LocalDateTime correctedLeaveDateTime = LocalDateTime.of(correctedLeaveDate, leaveDateTime.toLocalTime());
                    long correctedMinutes = ChronoUnit.MINUTES.between(admissionDateTime, correctedLeaveDateTime);
                    
                    // 如果修正后的时间差合理（不超过48小时），使用修正后的值
                    if (correctedMinutes >= 0 && correctedMinutes <= 2880) {
                        return correctedMinutes;
                    }
                }
                
                // 如果无法修正或修正后仍不合理，返回null（不参与计算）
                return null;
            }
            
            return minutes;
        } catch (Exception e) {
            // 如果解析失败，返回null
            return null;
        }
    }
    
    /**
     * 解析日期时间
     * @param dateStr 日期字符串（YYYY-MM-DD）
     * @param timeStr 时间字符串（4位，如"2326"表示23:26）
     * @return LocalDateTime对象，如果解析失败返回null
     */
    private LocalDateTime parseDateTime(String dateStr, String timeStr) {
        try {
            if (dateStr == null || timeStr == null || timeStr.length() != 4) {
                return null;
            }
            
            // 解析日期
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            
            // 解析时间：将4位字符串（如"2326"）转换为小时和分钟
            int hour = Integer.parseInt(timeStr.substring(0, 2));
            int minute = Integer.parseInt(timeStr.substring(2, 4));
            
            // 验证时间有效性
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return null;
            }
            
            return LocalDateTime.of(date, java.time.LocalTime.of(hour, minute));
        } catch (Exception e) {
            return null;
        }
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
    public List<Map<String, Object>> getInjuryCauseDistributionData(Integer year, String startDate, String endDate, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        
        // 不设置默认值，只在传递了对应参数时才添加查询条件
        // year、startDate、endDate、season、timePeriod、customStartTime、customEndTime 如果为 null，则不在SQL中添加对应的查询条件
        
        List<Map<String, Object>> rawData = patientStatisticsMapper.getInjuryCauseDistributionData(year, startDate, endDate, season, timePeriod, customStartTime, customEndTime);
        
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
    public List<Map<String, Object>> getISSDistributionData(String startDate, String endDate, Integer year, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        
        // 在Service层处理时间转换和参数验证
        Integer customStartTimeMinutes = null;
        Integer customEndTimeMinutes = null;
        if (customStartTime != null && customEndTime != null) {
            customStartTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customStartTime);
            customEndTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customEndTime);
        }
        
        // 查询原始数据，业务逻辑在Service层处理
        List<Map<String, Object>> rawData = patientStatisticsMapper.getISSDistributionData(
            startDate, endDate, year, season, timePeriod, 
            customStartTimeMinutes, customEndTimeMinutes
        );
        
        // 在Service层处理ISS分布数据，转换为饼状图格式
        return processISSDistributionData(rawData);
    }
    
    /**
     * 处理ISS分布数据，转换为饼状图格式
     * 业务逻辑：根据ISS评分判断伤情等级
     * 返回数据格式：name (名称), color (颜色), value (患者数量), percentage (百分比)
     * @param rawData 原始数据（包含 patient_id, iss_score 等字段）
     * @return 处理后的饼状图数据
     */
    private List<Map<String, Object>> processISSDistributionData(List<Map<String, Object>> rawData) {
        // 首先对患者进行去重处理，因为同一个患者可能有多条记录（由于JOIN interventiontime表）
        // 使用LinkedHashMap保持插入顺序，以patient_id为key去重
        Map<Object, Map<String, Object>> uniquePatients = new LinkedHashMap<>();
        for (Map<String, Object> record : rawData) {
            Object patientId = record.get("patient_id");
            // 如果该患者还未被记录，或者当前记录的ISS评分不为空，则更新
            if (!uniquePatients.containsKey(patientId)) {
                uniquePatients.put(patientId, record);
            }
        }
        
        // 初始化所有等级的数据计数
        // 轻度损伤 (ISS≤16)
        // 重度损伤 (16 < ISS ≤ 25)
        // 危重损伤 (ISS > 25)
        Map<String, Long> categoryCounts = new HashMap<>();
        categoryCounts.put("light", 0L);      // 轻度损伤
        categoryCounts.put("severe", 0L);     // 重度损伤
        categoryCounts.put("critical", 0L);   // 危重损伤
        
        // 收集各等级的患者信息，使用LinkedHashMap确保去重且保持顺序
        Map<Object, Map<String, Object>> lightPatientsMap = new LinkedHashMap<>();      // 轻度损伤患者
        Map<Object, Map<String, Object>> severePatientsMap = new LinkedHashMap<>();     // 重度损伤患者
        Map<Object, Map<String, Object>> criticalPatientsMap = new LinkedHashMap<>();  // 危重损伤患者
        
        // 在Service层处理业务逻辑：根据ISS评分判断伤情等级
        // 注意：只计算有ISS评分的记录，NULL值不参与计算
        // 现在使用去重后的患者数据
        for (Map<String, Object> record : uniquePatients.values()) {
            Object issScoreObj = record.get("iss_score");
            
            // 跳过NULL值，不参与计算
            if (issScoreObj == null) {
                continue;
            }
            
            Integer issScore = ((Number) issScoreObj).intValue();
            Object patientId = record.get("patient_id");
            
            // 业务规则判断：ISS分段
            String category;
            Map<String, Object> patientInfo = new HashMap<>();
            patientInfo.put("patient_id", patientId);
            patientInfo.put("iss_score", issScore);
            
            if (issScore <= 16) {
                category = "light";      // 轻度损伤
                // 收集 ISS ≤ 16 的患者信息，使用patient_id作为key确保去重
                if (!lightPatientsMap.containsKey(patientId)) {
                    lightPatientsMap.put(patientId, patientInfo);
                }
            } else if (issScore <= 25) {
                category = "severe";     // 重度损伤
                // 收集 16 < ISS ≤ 25 的患者信息，使用patient_id作为key确保去重
                if (!severePatientsMap.containsKey(patientId)) {
                    severePatientsMap.put(patientId, patientInfo);
                }
            } else {
                category = "critical";   // 危重损伤
                // 收集 ISS > 25 的患者信息，使用patient_id作为key确保去重
                if (!criticalPatientsMap.containsKey(patientId)) {
                    criticalPatientsMap.put(patientId, patientInfo);
                }
            }
            
            categoryCounts.put(category, categoryCounts.get(category) + 1);
        }
        
        // 计算总患者数
        long totalPatients = categoryCounts.values().stream().mapToLong(Long::longValue).sum();
        
        // 构建饼状图数据 - 确保返回所有三个等级
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 定义颜色和名称映射
        Map<String, Map<String, String>> categoryInfo = new HashMap<>();
        Map<String, String> lightInfo = new HashMap<>();
        lightInfo.put("name", "轻度损伤 (ISS≤16)");
        lightInfo.put("color", "#52C41A");
        categoryInfo.put("light", lightInfo);
        
        Map<String, String> severeInfo = new HashMap<>();
        severeInfo.put("name", "重度损伤 (16<ISS≤25)");
        severeInfo.put("color", "#FA8C16");
        categoryInfo.put("severe", severeInfo);
        
        Map<String, String> criticalInfo = new HashMap<>();
        criticalInfo.put("name", "危重损伤 (ISS>25)");
        criticalInfo.put("color", "#F5222D");
        categoryInfo.put("critical", criticalInfo);
        
        // 按顺序添加三个等级的数据
        String[] categories = {"light", "severe", "critical"};
        for (String category : categories) {
            Long patientCount = categoryCounts.get(category);
            double percentage = totalPatients > 0 ? 
                Math.round(patientCount * 100.0 / totalPatients * 100) / 100.0 : 0.0;
            
            Map<String, Object> pieData = new HashMap<>();
            pieData.put("name", categoryInfo.get(category).get("name"));
            pieData.put("color", categoryInfo.get(category).get("color"));
            pieData.put("value", patientCount);
            pieData.put("percentage", percentage);
            
            result.add(pieData);
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getGCSDistributionData(String startDate, String endDate, Integer year, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        
        // 不设置默认值，只在传递了对应参数时才添加查询条件
        // startDate、endDate、year、season、timePeriod、customStartTime、customEndTime 如果为 null，则不在SQL中添加对应的查询条件
        
        // 获取GCS分布数据
        List<Map<String, Object>> rawData = patientStatisticsMapper.getGCSDistributionData(startDate, endDate, year, season, timePeriod, customStartTime, customEndTime);
        
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
    public List<Map<String, Object>> getRTSDistributionData(String startDate, String endDate, Integer year, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        
        // 获取RTS分布数据
        List<Map<String, Object>> rawData = patientStatisticsMapper.getRTSDistributionData(startDate, endDate, year, season, timePeriod, customStartTime, customEndTime);
        
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
            
            pieData.put("name", name);
            pieData.put("value", patientCount);
            pieData.put("color", color);
            pieData.put("percentage", Math.round(percentage * 100.0) / 100.0);
            
            result.add(pieData);
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getPopulationBodyHeatmapData(String startDate, String endDate, Integer year, Integer season, Integer timePeriod, Integer ageGroup, Integer gender, Integer severity, String customStartTime, String customEndTime) {
        
        // 不设置默认值，只在传递了对应参数时才添加查询条件
        // startDate、endDate、year、season、timePeriod、ageGroup、gender、severity、customStartTime、customEndTime 如果为 null，则不在SQL中添加对应的查询条件
        
        List<Map<String, Object>> rawData = patientStatisticsMapper.getPopulationBodyHeatmapData(startDate, endDate, year, season, timePeriod, ageGroup, gender, severity, customStartTime, customEndTime);
        
        return rawData;
    }
    
    @Override
    public List<Integer> getDeathPatientIds(String startDate, String endDate, Integer year, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        // 不设置默认值，只在传递了对应参数时才添加查询条件
        // startDate、endDate、year、season、timePeriod、customStartTime、customEndTime 如果为 null，则不在SQL中添加对应的查询条件
        
        return patientStatisticsMapper.getDeathPatientIds(startDate, endDate, year, season, timePeriod, customStartTime, customEndTime);
    }
    
    @Override
    public List<Integer> getInjuryCausePatientIds(Integer injuryCauseCategory, String startDate, String endDate, Integer year, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        // 不设置默认值，只在传递了对应参数时才添加查询条件
        // injuryCauseCategory、startDate、endDate、year、season、timePeriod、customStartTime、customEndTime 如果为 null，则不在SQL中添加对应的查询条件
        
        return patientStatisticsMapper.getInjuryCausePatientIds(injuryCauseCategory, startDate, endDate, year, season, timePeriod, customStartTime, customEndTime);
    }
    
    @Override
    public List<Integer> getISSSegmentPatientIds(String issSegment, String startDate, String endDate, Integer year, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        // 在Service层处理时间转换和参数验证，与 getISSDistributionData 保持一致
        Integer customStartTimeMinutes = null;
        Integer customEndTimeMinutes = null;
        if (customStartTime != null && customEndTime != null) {
            customStartTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customStartTime);
            customEndTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customEndTime);
        }
        
        // 获取患者ID列表
        List<Integer> patientIds = patientStatisticsMapper.getISSSegmentPatientIds(issSegment, startDate, endDate, year, season, timePeriod, customStartTimeMinutes, customEndTimeMinutes);
        
        // 在Service层进行去重处理，因为同一个患者可能有多条记录（由于JOIN多张表）
        // 使用LinkedHashSet保持插入顺序并去重
        LinkedHashSet<Integer> uniquePatientIds = new LinkedHashSet<>(patientIds);
        
        return new ArrayList<>(uniquePatientIds);
    }
    
    @Override
    public List<Integer> getGCSSegmentPatientIds(String gcsSegment, String startDate, String endDate, Integer year, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        // 在Service层处理时间转换和参数验证，与 getGCSDistributionData 保持一致
        Integer customStartTimeMinutes = null;
        Integer customEndTimeMinutes = null;
        if (customStartTime != null && customEndTime != null) {
            customStartTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customStartTime);
            customEndTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customEndTime);
        }
        
        // 获取患者ID列表
        List<Integer> patientIds = patientStatisticsMapper.getGCSSegmentPatientIds(gcsSegment, startDate, endDate, year, season, timePeriod, customStartTimeMinutes, customEndTimeMinutes);
        
        // 在Service层进行去重处理，因为同一个患者可能有多条记录（由于JOIN多张表）
        // 使用LinkedHashSet保持插入顺序并去重，与ISS处理方式保持一致
        LinkedHashSet<Integer> uniquePatientIds = new LinkedHashSet<>(patientIds);
        
        return new ArrayList<>(uniquePatientIds);
    }

    @Override
    public List<Integer> getRTSScorePatientIds(Integer rtsScore, String startDate, String endDate, Integer year, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        // 在Service层处理时间转换和参数验证，与 getRTSDistributionData 保持一致
        Integer customStartTimeMinutes = null;
        Integer customEndTimeMinutes = null;
        if (customStartTime != null && customEndTime != null) {
            customStartTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customStartTime);
            customEndTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customEndTime);
        }
        
        // 获取患者ID列表
        List<Integer> patientIds = patientStatisticsMapper.getRTSScorePatientIds(rtsScore, startDate, endDate, year, season, timePeriod, customStartTimeMinutes, customEndTimeMinutes);
        
        // 在Service层进行去重处理，因为同一个患者可能有多条记录（由于JOIN多张表）
        // 使用LinkedHashSet保持插入顺序并去重，与GCS处理方式保持一致
        LinkedHashSet<Integer> uniquePatientIds = new LinkedHashSet<>(patientIds);
        
        return new ArrayList<>(uniquePatientIds);
    }
    
    @Override
    public List<Integer> getBodyPartPatientIds(String bodyPart, String startDate, String endDate, Integer year, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        // 在Service层处理时间转换和参数验证
        Integer customStartTimeMinutes = null;
        Integer customEndTimeMinutes = null;
        if (customStartTime != null && customEndTime != null) {
            customStartTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customStartTime);
            customEndTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customEndTime);
        }
        
        // 获取患者ID列表
        List<Integer> patientIds = patientStatisticsMapper.getBodyPartPatientIds(bodyPart, startDate, endDate, year, season, timePeriod, customStartTimeMinutes, customEndTimeMinutes);
        
        // 在Service层进行去重处理，因为同一个患者可能有多条记录（由于JOIN多张表）
        // 使用LinkedHashSet保持插入顺序并去重
        LinkedHashSet<Integer> uniquePatientIds = new LinkedHashSet<>(patientIds);
        
        return new ArrayList<>(uniquePatientIds);
    }
    
    @Override
    public List<Integer> getBodyRegionSeverityPatientIds(String bodyRegion, String severityLevel, String startDate, String endDate, Integer year, Integer season, Integer timePeriod, String customStartTime, String customEndTime) {
        // 在Service层处理时间转换和参数验证
        Integer customStartTimeMinutes = null;
        Integer customEndTimeMinutes = null;
        if (customStartTime != null && customEndTime != null) {
            customStartTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customStartTime);
            customEndTimeMinutes = com.demo.utils.TimeConversionUtils.convertHHmmToMinutes(customEndTime);
        }
        
        // 获取患者ID列表
        List<Integer> patientIds = patientStatisticsMapper.getBodyRegionSeverityPatientIds(bodyRegion, severityLevel, startDate, endDate, year, season, timePeriod, customStartTimeMinutes, customEndTimeMinutes);
        
        // 在Service层进行去重处理，因为同一个患者可能有多条记录（由于JOIN多张表）
        // 使用LinkedHashSet保持插入顺序并去重
        LinkedHashSet<Integer> uniquePatientIds = new LinkedHashSet<>(patientIds);
        
        return new ArrayList<>(uniquePatientIds);
    }
    
    /**
     * 获取季节名称
     */
    private String getSeasonName(Integer season) {
        if (season == null) return "全部";
        switch (season) {
            case 0: return "春季";
            case 1: return "夏季";
            case 2: return "秋季";
            case 3: return "冬季";
            default: return "未知(" + season + ")";
        }
    }
    
    /**
     * 获取时间段名称
     */
    private String getTimePeriodName(Integer timePeriod) {
        if (timePeriod == null) return "全部";
        switch (timePeriod) {
            case 0: return "夜间(0-7时)";
            case 1: return "早高峰(8-9时)";
            case 2: return "午高峰(10-11时)";
            case 3: return "下午(12-16时)";
            case 4: return "晚高峰(17-19时)";
            case 5: return "晚上(20-23时)";
            default: return "未知(" + timePeriod + ")";
        }
    }
}
