package com.demo.Service.impl.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.entity.InterventionTime;
import com.demo.mapper.InterventionTimeMapper;
import com.demo.Service.impl.IInterventionTimeService;
import com.demo.dto.TimelineEventDTO;
import com.demo.dto.TimelineStatisticsDTO;
import com.demo.dto.AllKeyEventsStatisticsDTO;
import com.demo.dto.KeyEventStatisticsDTO;
import com.demo.dto.DataErrorDTO;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterventionTimeServiceImpl extends ServiceImpl<InterventionTimeMapper, InterventionTime> implements IInterventionTimeService {


    @Override
    public List<InterventionTime> getByPatientId(Integer patientId) {
        return baseMapper.selectByPatientId(patientId);
    }

    @Override
    public List<TimelineEventDTO> getTimelineEvents(Integer patientId) {
        List<InterventionTime> interventions = getByPatientId(patientId);
        if (interventions.isEmpty()) {
            return new ArrayList<>();
        }

        List<TimelineEventDTO> events = new ArrayList<>();
        
        // 关键事件列表
        events.addAll(getKeyEvents(patientId));
        // 非关键事件列表
        events.addAll(getNonKeyEvents(patientId));
        
        // 按时间排序
        return events.stream()
                .sorted(Comparator.comparing(TimelineEventDTO::getEventTime))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimelineEventDTO> getKeyEvents(Integer patientId) {
        List<InterventionTime> interventions = getByPatientId(patientId);
        if (interventions.isEmpty()) {
            return new ArrayList<>();
        }

        InterventionTime intervention = interventions.get(0);
        List<TimelineEventDTO> keyEvents = new ArrayList<>();
        LocalDate admissionDate = intervention.getAdmissionDate();
        
        // 入室事件
        if (intervention.getAdmissionTime() != null) {
            addEventIfValid(keyEvents, createEvent("入室", admissionDate, intervention.getAdmissionTime(), 
                "key", "admission", "患者进入抢救室", "el-icon-office-building", "#409EFF", 1));
        }
        
        // CT检查
        if (intervention.getCT() != null) {
            addEventIfValid(keyEvents, createEvent("CT", admissionDate, intervention.getCT(), 
                "key", "examination", "CT检查", "el-icon-camera", "#909399", 2));
        }
        
        // 气管插管
        if (intervention.getEndotrachealTube() != null) {
            addEventIfValid(keyEvents, createEvent("气管插管", admissionDate, intervention.getEndotrachealTube(), 
                "key", "intervention", "气管插管", "el-icon-help", "#E6A23C", 3));
        }
        
        // 输血开始
        if (intervention.getTransfusionStart() != null) {
            addEventIfValid(keyEvents, createEvent("输血开始", admissionDate, intervention.getTransfusionStart(), 
                "key", "treatment", "输血开始", "el-icon-watermelon", "#F56C6C", 4));
        }
        
        // 输血结束 - 已移动到非关键事件列表
        
        // 离室
        if (intervention.getLeaveSurgeryTime() != null) {
            LocalDate leaveDate = intervention.getLeaveSurgeryDate() != null ? 
                intervention.getLeaveSurgeryDate() : admissionDate;
            addEventIfValid(keyEvents, createEventWithDestination("离室", leaveDate, intervention.getLeaveSurgeryTime(), 
                "key", "discharge", "离开抢救室", "el-icon-position", "#409EFF", 6, intervention.getPatientDestination()));
        }
        
        // 死亡
        if ("是".equals(intervention.getDeath()) && intervention.getDeathTime() != null) {
            LocalDate deathDate = intervention.getDeathDate() != null ? 
                intervention.getDeathDate() : admissionDate;
            addEventIfValid(keyEvents, createEvent("死亡", deathDate, intervention.getDeathTime(), 
                "key", "death", "患者死亡", "el-icon-warning", "#F56C6C", 7));
        }
        
        return keyEvents;
    }

    @Override
    public List<TimelineEventDTO> getNonKeyEvents(Integer patientId) {
        List<InterventionTime> interventions = getByPatientId(patientId);
        if (interventions.isEmpty()) {
            return new ArrayList<>();
        }

        InterventionTime intervention = interventions.get(0);
        List<TimelineEventDTO> nonKeyEvents = new ArrayList<>();
        LocalDate admissionDate = intervention.getAdmissionDate();
        
        // 外周静脉
        if (intervention.getPeripheral() != null) {
            addEventIfValid(nonKeyEvents, createEvent("外周静脉", admissionDate, intervention.getPeripheral(), 
                "non_key", "treatment", "外周静脉通路", "el-icon-first-aid-kit", "#67C23A", 1));
        }
        
        // 深静脉
        if (intervention.getIvLine() != null) {
            addEventIfValid(nonKeyEvents, createEvent("深静脉", admissionDate, intervention.getIvLine(), 
                "non_key", "treatment", "深静脉通路", "el-icon-first-aid-kit", "#67C23A", 2));
        }
        
        // 骨通道
        if (intervention.getCentralAccess() != null) {
            addEventIfValid(nonKeyEvents, createEvent("骨通道", admissionDate, intervention.getCentralAccess(), 
                "non_key", "treatment", "骨通道建立", "el-icon-set-up", "#67C23A", 3));
        }
        
        // 鼻导管
        if (intervention.getNasalPipe() != null) {
            addEventIfValid(nonKeyEvents, createEvent("鼻导管", admissionDate, intervention.getNasalPipe(), 
                "non_key", "treatment", "鼻导管给氧", "el-icon-wind-power", "#67C23A", 4));
        }
        
        // 面罩
        if (intervention.getFaceMask() != null) {
            addEventIfValid(nonKeyEvents, createEvent("面罩", admissionDate, intervention.getFaceMask(), 
                "non_key", "treatment", "面罩给氧", "el-icon-mask", "#67C23A", 5));
        }
        
        // 输血结束 - 从关键事件改为非关键事件
        if (intervention.getTransfusionEnd() != null) {
            addEventIfValid(nonKeyEvents, createEvent("输血结束", admissionDate, intervention.getTransfusionEnd(), 
                "non_key", "treatment", "输血结束", "el-icon-watermelon", "#F56C6C", 6));
        }
        
        // 呼吸机
        if (intervention.getVentilator() != null) {
            addEventIfValid(nonKeyEvents, createEvent("呼吸机", admissionDate, intervention.getVentilator(), 
                "non_key", "treatment", "呼吸机使用", "el-icon-c-scale-to-original", "#E6A23C", 7));
        }
        
        // 心肺复苏开始
        if (intervention.getCprStartTime() != null) {
            addEventIfValid(nonKeyEvents, createEvent("心肺复苏开始", admissionDate, intervention.getCprStartTime(), 
                "non_key", "emergency", "心肺复苏开始", "el-icon-first-aid-kit", "#F56C6C", 8));
        }
        
        // 心肺复苏结束
        if (intervention.getCprEndTime() != null) {
            addEventIfValid(nonKeyEvents, createEvent("心肺复苏结束", admissionDate, intervention.getCprEndTime(), 
                "non_key", "emergency", "心肺复苏结束", "el-icon-first-aid-kit", "#F56C6C", 9));
        }
        
        // B超
        if (intervention.getUltrasound() != null) {
            addEventIfValid(nonKeyEvents, createEvent("B超", admissionDate, intervention.getUltrasound(), 
                "non_key", "examination", "B超检查", "el-icon-video-camera", "#909399", 10));
        }
        
        // 止血带
        if (intervention.getTourniquet() != null) {
            addEventIfValid(nonKeyEvents, createEvent("止血带", admissionDate, intervention.getTourniquet(), 
                "non_key", "treatment", "止血带使用", "el-icon-warning-outline", "#E6A23C", 11));
        }
        
        // 采血
        if (intervention.getBloodDraw() != null) {
            addEventIfValid(nonKeyEvents, createEvent("采血", admissionDate, intervention.getBloodDraw(), 
                "non_key", "examination", "采血检查", "el-icon-document", "#F56C6C", 12));
        }
        
        // 导尿
        if (intervention.getCatheter() != null) {
            addEventIfValid(nonKeyEvents, createEvent("导尿", admissionDate, intervention.getCatheter(), 
                "non_key", "treatment", "导尿操作", "el-icon-connection", "#67C23A", 13));
        }
        
        // 胃管
        if (intervention.getGastricTube() != null) {
            addEventIfValid(nonKeyEvents, createEvent("胃管", admissionDate, intervention.getGastricTube(), 
                "non_key", "treatment", "胃管置入", "el-icon-food", "#67C23A", 14));
        }
        
        return nonKeyEvents;
    }

    @Override
    public TimelineStatisticsDTO getEventStatistics(String eventType) {
        return getEventStatistics(eventType, null);
    }
    
    /**
     * 获取事件统计信息（支持指定当前患者ID）
     */
    public TimelineStatisticsDTO getEventStatistics(String eventType, Integer currentPatientId) {
        // 获取所有患者的历史数据
        List<InterventionTime> allInterventions = baseMapper.selectAll();
        
        // 计算该事件类型与入室时间的差值
        List<Double> timeDifferences = new ArrayList<>();
        Double currentPatientTime = null;
        
        for (InterventionTime intervention : allInterventions) {
            // 获取入室时间
            LocalDateTime admissionTime = parseDateTime(intervention.getAdmissionDate(), intervention.getAdmissionTime());
            if (admissionTime == null) continue;
            
            // 根据事件类型获取对应时间
            LocalDateTime eventTime = null;
            switch (eventType.toLowerCase()) {
                case "admission":
                    eventTime = admissionTime;
                    break;
                case "ct":
                    eventTime = parseDateTime(intervention.getAdmissionDate(), intervention.getCT());
                    break;
                case "intubation":
                    eventTime = parseDateTime(intervention.getAdmissionDate(), intervention.getEndotrachealTube());
                    break;
                case "transfusion":
                    eventTime = parseDateTime(intervention.getAdmissionDate(), intervention.getTransfusionStart());
                    break;
                case "discharge":
                    LocalDate departureDate = intervention.getLeaveSurgeryDate() != null ? 
                        intervention.getLeaveSurgeryDate() : intervention.getAdmissionDate();
                    eventTime = parseDateTime(departureDate, intervention.getLeaveSurgeryTime());
                    break;
                case "death":
                    LocalDate deathDate = intervention.getDeathDate() != null ? 
                        intervention.getDeathDate() : intervention.getAdmissionDate();
                    eventTime = parseDateTime(deathDate, intervention.getDeathTime());
                    break;
            }
            
            // 如果事件时间为null（无效时间），跳过此记录
            if (eventTime == null) continue;
            
            // 计算时间差（分钟）= 关键事件时间 - 入室时间
            long minutes = Duration.between(admissionTime, eventTime).toMinutes();
            timeDifferences.add((double) minutes);
            
            // 如果是当前患者，记录其时间
            if (currentPatientId != null && intervention.getPatientId().equals(currentPatientId)) {
                currentPatientTime = (double) minutes;
            }
        }
        
        if (timeDifferences.isEmpty()) {
            return createEmptyStatistics(eventType);
        }
        
        // 计算统计值
        double meanTime = timeDifferences.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double standardDeviation = calculateStandardDeviation(timeDifferences, meanTime);
        double medianTime = calculateMedian(timeDifferences);
        
        // 质控标准线 = 均值 - 1个标准差（但确保不为负数）
        // 如果标准差过大导致质控线为负，则设为0
        double qualityControlLine = Math.max(0, meanTime - standardDeviation);
        
        // 如果没有指定当前患者，使用第一个患者作为示例
        if (currentPatientTime == null) {
            currentPatientTime = timeDifferences.get(0);
        }
        
        TimelineStatisticsDTO stats = new TimelineStatisticsDTO();
        stats.setEventType(eventType);
        stats.setMeanTime(meanTime);
        stats.setMedianTime(medianTime);
        stats.setStandardDeviation(standardDeviation);
        stats.setCurrentPatientTime(currentPatientTime);
        stats.setQualityControlLine(qualityControlLine);
        
        // 生成正态分布曲线数据点
        List<Double> points = generateNormalDistributionPoints(meanTime, standardDeviation);
        stats.setDistributionPoints(points);
        
        return stats;
    }

    // 辅助方法：创建事件
    private TimelineEventDTO createEvent(String name, LocalDate date, String timeStr, 
                                       String type, String group, String description,
                                       String icon, String color, Integer sortOrder) {
        LocalDateTime eventTime = parseDateTime(date, timeStr);
        // 如果时间为null（无效时间），返回null，不创建事件
        if (eventTime == null) {
            return null;
        }
        
        TimelineEventDTO event = new TimelineEventDTO();
        event.setEventName(name);
        event.setEventTime(eventTime);
        event.setEventType(type);
        event.setEventGroup(group);
        event.setDescription(description);
        event.setIcon(icon);
        event.setColor(color);
        event.setSortOrder(sortOrder);
        return event;
    }
    
    // 辅助方法：创建带去向的事件（用于离室事件）
    private TimelineEventDTO createEventWithDestination(String name, LocalDate date, String timeStr, 
                                                       String type, String group, String description,
                                                       String icon, String color, Integer sortOrder, String destination) {
        LocalDateTime eventTime = parseDateTime(date, timeStr);
        // 如果时间为null（无效时间），返回null，不创建事件
        if (eventTime == null) {
            return null;
        }
        
        TimelineEventDTO event = new TimelineEventDTO();
        event.setEventName(name);
        event.setEventTime(eventTime);
        event.setEventType(type);
        event.setEventGroup(group);
        event.setDescription(description);
        event.setIcon(icon);
        event.setColor(color);
        event.setSortOrder(sortOrder);
        event.setDestination(destination);
        return event;
    }
    
    // 辅助方法：安全添加事件（过滤null事件）
    private void addEventIfValid(List<TimelineEventDTO> eventList, TimelineEventDTO event) {
        if (event != null) {
            eventList.add(event);
        }
    }

    // 辅助方法：解析时间字符串为LocalDateTime
    private LocalDateTime parseDateTime(LocalDate date, String timeStr) {
        if (timeStr == null || timeStr.length() != 4) {
            return null; // 返回null表示无效时间
        }
        
        try {
            // 支持跨天编码：如果为 >= 2400，表示第二天（值减去2400）
            int numeric = Integer.parseInt(timeStr);
            LocalDate targetDate = date;
            if (numeric >= 2400) {
                numeric = numeric - 2400;
                targetDate = date.plusDays(1);
            }
            int hour = numeric / 100;
            int minute = numeric % 100;
            if (hour > 23 || minute > 59) {
                return null;
            }
            LocalTime time = LocalTime.of(hour, minute);
            return LocalDateTime.of(targetDate, time);
        } catch (NumberFormatException e) {
            return null; // 返回null表示无效时间
        }
    }

    // 辅助方法：生成正态分布曲线数据点
    private List<Double> generateNormalDistributionPoints(double mean, double stdDev) {
        List<Double> points = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            double x = (i - 50) * 2.0; // -100 到 100
            double y = Math.exp(-0.5 * Math.pow((x - mean) / stdDev, 2)) / (stdDev * Math.sqrt(2 * Math.PI));
            points.add(y);
        }
        return points;
    }
    
    // 计算标准差
    private double calculateStandardDeviation(List<Double> values, double mean) {
        double sumSquaredDiffs = values.stream()
            .mapToDouble(value -> Math.pow(value - mean, 2))
            .sum();
        return Math.sqrt(sumSquaredDiffs / values.size());
    }
    
    // 计算中位数
    private double calculateMedian(List<Double> values) {
        List<Double> sortedValues = new ArrayList<>(values);
        Collections.sort(sortedValues);
        int size = sortedValues.size();
        if (size % 2 == 0) {
            return (sortedValues.get(size / 2 - 1) + sortedValues.get(size / 2)) / 2.0;
        } else {
            return sortedValues.get(size / 2);
        }
    }
    
    // 创建空统计信息
    private TimelineStatisticsDTO createEmptyStatistics(String eventType) {
        TimelineStatisticsDTO stats = new TimelineStatisticsDTO();
        stats.setEventType(eventType);
        stats.setMeanTime(0.0);
        stats.setMedianTime(0.0);
        stats.setStandardDeviation(0.0);
        stats.setCurrentPatientTime(0.0);
        stats.setQualityControlLine(0.0);
        stats.setDistributionPoints(new ArrayList<>());
        return stats;
    }
    
    /**
     * 获取所有关键事件的正态分布统计信息
     */
    @Override
    public AllKeyEventsStatisticsDTO getAllKeyEventsStatistics() {
        // 定义关键事件列表：事件名称、事件类型key、获取事件时间的方法
        Map<String, EventInfo> keyEventsMap = new LinkedHashMap<>();
        keyEventsMap.put("入室", new EventInfo("admission", (intervention, admissionDate) -> 
            parseDateTime(intervention.getAdmissionDate(), intervention.getAdmissionTime())));
        keyEventsMap.put("CT", new EventInfo("ct", (intervention, admissionDate) -> 
            parseDateTime(intervention.getAdmissionDate(), intervention.getCT())));
        keyEventsMap.put("气管插管", new EventInfo("intubation", (intervention, admissionDate) -> 
            parseDateTime(intervention.getAdmissionDate(), intervention.getEndotrachealTube())));
        keyEventsMap.put("输血开始", new EventInfo("transfusion", (intervention, admissionDate) -> 
            parseDateTime(intervention.getAdmissionDate(), intervention.getTransfusionStart())));
        keyEventsMap.put("离室", new EventInfo("discharge", (intervention, admissionDate) -> {
            LocalDate leaveDate = intervention.getLeaveSurgeryDate() != null ? 
                intervention.getLeaveSurgeryDate() : intervention.getAdmissionDate();
            return parseDateTime(leaveDate, intervention.getLeaveSurgeryTime());
        }));
        keyEventsMap.put("死亡", new EventInfo("death", (intervention, admissionDate) -> {
            LocalDate deathDate = intervention.getDeathDate() != null ? 
                intervention.getDeathDate() : intervention.getAdmissionDate();
            return parseDateTime(deathDate, intervention.getDeathTime());
        }));
        
        // 获取所有患者数据
        List<InterventionTime> allInterventions = baseMapper.selectAll();
        
        // 存储所有错误数据
        List<DataErrorDTO> allErrorData = new ArrayList<>();
        
        // 存储所有关键事件的统计信息
        List<KeyEventStatisticsDTO> eventStatisticsList = new ArrayList<>();
        
        // 对每个关键事件进行处理
        for (Map.Entry<String, EventInfo> entry : keyEventsMap.entrySet()) {
            String eventName = entry.getKey();
            EventInfo eventInfo = entry.getValue();
            
            // 存储有效的时间差数据
            List<Double> validTimeDifferences = new ArrayList<>();
            
            // 遍历所有患者数据
            for (InterventionTime intervention : allInterventions) {
                // 获取入室时间
                LocalDateTime admissionTime = parseDateTime(
                    intervention.getAdmissionDate(), 
                    intervention.getAdmissionTime()
                );
                if (admissionTime == null) continue;
                
                // 获取关键事件时间
                LocalDateTime eventTime = eventInfo.getEventTimeFunction.apply(intervention, intervention.getAdmissionDate());
                if (eventTime == null) continue;
                
                // 计算时间差（分钟）
                long minutes = Duration.between(admissionTime, eventTime).toMinutes();
                
                // 检查是否为错误数据：时间差超过48小时（2880分钟）或为负数
                if (minutes < 0 || minutes > 2880) {
                    // 记录错误数据
                    DataErrorDTO error = new DataErrorDTO();
                    error.setPatientId(intervention.getPatientId());
                    error.setEventName(eventName);
                    error.setAdmissionTime(formatDateTime(admissionTime));
                    error.setEventTime(formatDateTime(eventTime));
                    error.setTimeDifferenceMinutes(minutes);
                    if (minutes < 0) {
                        error.setErrorReason("时间差为负数（关键事件时间早于入室时间）");
                    } else {
                        error.setErrorReason("时间差超过48小时");
                    }
                    allErrorData.add(error);
                    // 跳过此数据，不参与计算
                    continue;
                }
                
                // 有效数据，添加到列表
                validTimeDifferences.add((double) minutes);
            }
            
            // 如果有效数据为空，创建空统计信息
            if (validTimeDifferences.isEmpty()) {
                KeyEventStatisticsDTO emptyStats = new KeyEventStatisticsDTO();
                emptyStats.setEventName(eventName);
                emptyStats.setEventType(eventInfo.getEventType());
                emptyStats.setMeanTime(0.0);
                emptyStats.setStandardDeviation(0.0);
                emptyStats.setMedianTime(0.0);
                emptyStats.setQualityControlLine(0.0);
                emptyStats.setValidDataCount(0);
                emptyStats.setDistributionCurve(null);
                emptyStats.setXAxisRange(null);
                eventStatisticsList.add(emptyStats);
                continue;
            }
            
            // 计算统计值
            double meanTime = validTimeDifferences.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
            double standardDeviation = calculateStandardDeviation(validTimeDifferences, meanTime);
            double medianTime = calculateMedian(validTimeDifferences);
            
            // 质控标准线 = 均值 - 1个标准差（但确保不为负数）
            double qualityControlLine = Math.max(0, meanTime - standardDeviation);
            
            // 不再生成分布曲线数据点，前端根据均值和标准差自行绘制
            // 创建统计DTO
            KeyEventStatisticsDTO stats = new KeyEventStatisticsDTO();
            stats.setEventName(eventName);
            stats.setEventType(eventInfo.getEventType());
            stats.setMeanTime(meanTime);
            stats.setStandardDeviation(standardDeviation);
            stats.setMedianTime(medianTime);
            stats.setQualityControlLine(qualityControlLine);
            stats.setValidDataCount(validTimeDifferences.size());
            // 不设置distributionCurve，前端根据均值和标准差自行计算
            stats.setDistributionCurve(null);
            // 不设置xAxisRange，前端根据需求自行计算（需要包含负半轴）
            stats.setXAxisRange(null);
            
            eventStatisticsList.add(stats);
        }
        
        // 保存错误数据到txt文件
        saveErrorDataToFile(allErrorData);
        
        // 创建返回DTO
        AllKeyEventsStatisticsDTO result = new AllKeyEventsStatisticsDTO();
        result.setEventStatistics(eventStatisticsList);
        result.setErrorData(allErrorData);
        result.setErrorCount(allErrorData.size());
        
        return result;
    }
    
    /**
     * 获取所有关键事件的正态分布统计信息（支持指定当前患者ID）
     */
    @Override
    public AllKeyEventsStatisticsDTO getAllKeyEventsStatistics(Integer currentPatientId) {
        // 定义关键事件列表：事件名称、事件类型key、获取事件时间的方法
        Map<String, EventInfo> keyEventsMap = new LinkedHashMap<>();
        keyEventsMap.put("入室", new EventInfo("admission", (intervention, admissionDate) -> 
            parseDateTime(intervention.getAdmissionDate(), intervention.getAdmissionTime())));
        keyEventsMap.put("CT", new EventInfo("ct", (intervention, admissionDate) -> 
            parseDateTime(intervention.getAdmissionDate(), intervention.getCT())));
        keyEventsMap.put("气管插管", new EventInfo("intubation", (intervention, admissionDate) -> 
            parseDateTime(intervention.getAdmissionDate(), intervention.getEndotrachealTube())));
        keyEventsMap.put("输血开始", new EventInfo("transfusion", (intervention, admissionDate) -> 
            parseDateTime(intervention.getAdmissionDate(), intervention.getTransfusionStart())));
        keyEventsMap.put("离室", new EventInfo("discharge", (intervention, admissionDate) -> {
            LocalDate leaveDate = intervention.getLeaveSurgeryDate() != null ? 
                intervention.getLeaveSurgeryDate() : intervention.getAdmissionDate();
            return parseDateTime(leaveDate, intervention.getLeaveSurgeryTime());
        }));
        keyEventsMap.put("死亡", new EventInfo("death", (intervention, admissionDate) -> {
            LocalDate deathDate = intervention.getDeathDate() != null ? 
                intervention.getDeathDate() : intervention.getAdmissionDate();
            return parseDateTime(deathDate, intervention.getDeathTime());
        }));
        
        // 获取所有患者数据
        List<InterventionTime> allInterventions = baseMapper.selectAll();
        
        // 存储所有错误数据
        List<DataErrorDTO> allErrorData = new ArrayList<>();
        
        // 存储所有关键事件的统计信息
        List<KeyEventStatisticsDTO> eventStatisticsList = new ArrayList<>();
        
        // 对每个关键事件进行处理
        for (Map.Entry<String, EventInfo> entry : keyEventsMap.entrySet()) {
            String eventName = entry.getKey();
            EventInfo eventInfo = entry.getValue();
            
            // 存储有效的时间差数据
            List<Double> validTimeDifferences = new ArrayList<>();
            Double currentPatientTime = null;
            
            // 遍历所有患者数据
            for (InterventionTime intervention : allInterventions) {
                // 获取入室时间
                LocalDateTime admissionTime = parseDateTime(
                    intervention.getAdmissionDate(), 
                    intervention.getAdmissionTime()
                );
                if (admissionTime == null) continue;
                
                // 获取关键事件时间
                LocalDateTime eventTime = eventInfo.getEventTimeFunction.apply(intervention, intervention.getAdmissionDate());
                if (eventTime == null) continue;
                
                // 计算时间差（分钟）
                long minutes = Duration.between(admissionTime, eventTime).toMinutes();
                
                // 如果是当前患者，记录其时间（无论是否为错误数据都要记录）
                if (currentPatientId != null && intervention.getPatientId().equals(currentPatientId)) {
                    currentPatientTime = (double) minutes;
                }
                
                // 检查是否为错误数据：时间差超过48小时（2880分钟）或为负数
                if (minutes < 0 || minutes > 2880) {
                    // 记录错误数据
                    DataErrorDTO error = new DataErrorDTO();
                    error.setPatientId(intervention.getPatientId());
                    error.setEventName(eventName);
                    error.setAdmissionTime(formatDateTime(admissionTime));
                    error.setEventTime(formatDateTime(eventTime));
                    error.setTimeDifferenceMinutes(minutes);
                    if (minutes < 0) {
                        error.setErrorReason("时间差为负数（关键事件时间早于入室时间）");
                    } else {
                        error.setErrorReason("时间差超过48小时");
                    }
                    allErrorData.add(error);
                    // 跳过此数据，不参与计算
                    continue;
                }
                
                // 有效数据，添加到列表
                validTimeDifferences.add((double) minutes);
            }
            
            // 如果有效数据为空，创建空统计信息
            if (validTimeDifferences.isEmpty()) {
                KeyEventStatisticsDTO emptyStats = new KeyEventStatisticsDTO();
                emptyStats.setEventName(eventName);
                emptyStats.setEventType(eventInfo.getEventType());
                emptyStats.setMeanTime(0.0);
                emptyStats.setStandardDeviation(0.0);
                emptyStats.setMedianTime(0.0);
                emptyStats.setQualityControlLine(0.0);
                emptyStats.setValidDataCount(0);
                emptyStats.setDistributionCurve(null);
                emptyStats.setXAxisRange(null);
                emptyStats.setCurrentPatientTime(currentPatientTime);
                eventStatisticsList.add(emptyStats);
                continue;
            }
            
            // 计算统计值
            double meanTime = validTimeDifferences.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
            double standardDeviation = calculateStandardDeviation(validTimeDifferences, meanTime);
            double medianTime = calculateMedian(validTimeDifferences);
            
            // 质控标准线 = 均值 - 1个标准差（但确保不为负数）
            double qualityControlLine = Math.max(0, meanTime - standardDeviation);
            
            // 不再生成分布曲线数据点，前端根据均值和标准差自行绘制
            // 创建统计DTO
            KeyEventStatisticsDTO stats = new KeyEventStatisticsDTO();
            stats.setEventName(eventName);
            stats.setEventType(eventInfo.getEventType());
            stats.setMeanTime(meanTime);
            stats.setStandardDeviation(standardDeviation);
            stats.setMedianTime(medianTime);
            stats.setQualityControlLine(qualityControlLine);
            stats.setValidDataCount(validTimeDifferences.size());
            // 不设置distributionCurve，前端根据均值和标准差自行计算
            stats.setDistributionCurve(null);
            // 不设置xAxisRange，前端根据需求自行计算（需要包含负半轴）
            stats.setXAxisRange(null);
            stats.setCurrentPatientTime(currentPatientTime);
            
            eventStatisticsList.add(stats);
        }
        
        // 保存错误数据到txt文件
        saveErrorDataToFile(allErrorData);
        
        // 创建返回DTO
        AllKeyEventsStatisticsDTO result = new AllKeyEventsStatisticsDTO();
        result.setEventStatistics(eventStatisticsList);
        result.setErrorData(allErrorData);
        result.setErrorCount(allErrorData.size());
        
        return result;
    }
    
    /**
     * 格式化日期时间为字符串
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }
    
    /**
     * 保存错误数据到txt文件
     */
    private void saveErrorDataToFile(List<DataErrorDTO> errorData) {
        if (errorData.isEmpty()) {
            return;
        }
        
        try {
            // 创建文件路径（在项目根目录下）
            String filePath = "key_events_error_data.txt";
            
            // 写入文件
            try (FileWriter writer = new FileWriter(filePath, false)) {
                writer.write("关键事件错误数据记录\n");
                writer.write("生成时间: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
                writer.write(String.format("%100s\n\n", "").replace(' ', '='));
                
                for (DataErrorDTO error : errorData) {
                    writer.write(String.format("患者ID: %d\n", error.getPatientId()));
                    writer.write(String.format("事件名称: %s\n", error.getEventName()));
                    writer.write(String.format("入室时间: %s\n", error.getAdmissionTime()));
                    writer.write(String.format("事件时间: %s\n", error.getEventTime()));
                    writer.write(String.format("时间差: %d 分钟\n", error.getTimeDifferenceMinutes()));
                    writer.write(String.format("错误原因: %s\n", error.getErrorReason()));
                    writer.write(String.format("%100s\n\n", "").replace(' ', '-'));
                }
            }
        } catch (IOException e) {
            // 如果文件写入失败，记录日志但不影响主流程
            System.err.println("保存错误数据到文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 事件信息内部类
     */
    private static class EventInfo {
        private String eventType;
        private java.util.function.BiFunction<InterventionTime, LocalDate, LocalDateTime> getEventTimeFunction;
        
        public EventInfo(String eventType, 
                        java.util.function.BiFunction<InterventionTime, LocalDate, LocalDateTime> getEventTimeFunction) {
            this.eventType = eventType;
            this.getEventTimeFunction = getEventTimeFunction;
        }
        
        public String getEventType() {
            return eventType;
        }
    }
    
    /**
     * 根据患者ID查询单条干预时间记录（用于编辑回显）
     */
    @Override
    public InterventionTime getOneByPatientId(Integer patientId) {
        if (patientId == null || patientId <= 0) {
            return null;
        }
        return baseMapper.selectOneByPatientId(patientId);
    }
    
    /**
     * 更新干预时间记录
     * 使用 UpdateWrapper 来确保 null 值也能被更新（允许清空字段）
     * 针对事件的时间字段：如果值为"否"、"无"或空值，则转换为null；否则存储正确的时间
     */
    @Override
    public boolean updateInterventionTime(InterventionTime interventionTime) {
        if (interventionTime == null || interventionTime.getPatientId() == null) {
            return false;
        }
        
        // 处理所有时间字段：将"否"、"无"、空值转换为null
        processTimeFields(interventionTime);
        
        // 应用跨天偏移：入室时间早于事件时间规则（事件只有时间无日期）
        applyCrossDayOffsets(interventionTime);
        
        // 先查询是否存在该患者的记录
        InterventionTime existing = baseMapper.selectOneByPatientId(interventionTime.getPatientId());
        
        if (existing == null) {
            // 如果不存在，则插入新记录
            interventionTime.setInterventionId(null); // 确保ID为null，让数据库自动生成
            return save(interventionTime);
        } else {
            // 如果存在，则更新记录
            // 使用 UpdateWrapper 来显式更新所有字段，包括 null 值
            UpdateWrapper<InterventionTime> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("intervention_id", existing.getInterventionId());
            
            // 显式设置所有字段，包括 null 值，这样删除字段时才能正确更新为 null
            updateWrapper.set("patient_id", interventionTime.getPatientId());
            updateWrapper.set("admission_date", interventionTime.getAdmissionDate());
            updateWrapper.set("admission_time", interventionTime.getAdmissionTime());
            updateWrapper.set("peripheral", interventionTime.getPeripheral());
            updateWrapper.set("iv_line", interventionTime.getIvLine());
            updateWrapper.set("central_access", interventionTime.getCentralAccess());
            updateWrapper.set("nasal_pipe", interventionTime.getNasalPipe());
            updateWrapper.set("face_mask", interventionTime.getFaceMask());
            updateWrapper.set("endotracheal_tube", interventionTime.getEndotrachealTube());
            updateWrapper.set("ventilator", interventionTime.getVentilator());
            updateWrapper.set("cpr", interventionTime.getCpr());
            updateWrapper.set("cpr_start_time", interventionTime.getCprStartTime());
            updateWrapper.set("cpr_end_time", interventionTime.getCprEndTime());
            updateWrapper.set("ultrasound", interventionTime.getUltrasound());
            updateWrapper.set("ct", interventionTime.getCT());
            updateWrapper.set("tourniquet", interventionTime.getTourniquet());
            updateWrapper.set("blood_draw", interventionTime.getBloodDraw());
            updateWrapper.set("catheter", interventionTime.getCatheter());
            updateWrapper.set("gastric_tube", interventionTime.getGastricTube());
            updateWrapper.set("transfusion", interventionTime.getTransfusion());
            updateWrapper.set("transfusion_start", interventionTime.getTransfusionStart());
            updateWrapper.set("transfusion_end", interventionTime.getTransfusionEnd());
            updateWrapper.set("leave_surgery_time", interventionTime.getLeaveSurgeryTime());
            updateWrapper.set("leave_surgery_date", interventionTime.getLeaveSurgeryDate());
            updateWrapper.set("patient_destination", interventionTime.getPatientDestination());
            updateWrapper.set("death", interventionTime.getDeath());
            updateWrapper.set("death_date", interventionTime.getDeathDate());
            updateWrapper.set("death_time", interventionTime.getDeathTime());
            
            return update(updateWrapper);
        }
    }
    
    /**
     * 处理时间字段：将"否"、"无"、空值转换为null
     * 针对事件的时间字段，如果输入为"否"、"无"或空值，说明没有这一时间，设置为null
     * 否则保持原值（应该是正确的四位数字HHMM格式）
     */
    private void processTimeFields(InterventionTime interventionTime) {
        // 处理所有时间字段
        interventionTime.setAdmissionTime(normalizeTimeValue(interventionTime.getAdmissionTime()));
        interventionTime.setPeripheral(normalizeTimeValue(interventionTime.getPeripheral()));
        interventionTime.setIvLine(normalizeTimeValue(interventionTime.getIvLine()));
        interventionTime.setCentralAccess(normalizeTimeValue(interventionTime.getCentralAccess()));
        interventionTime.setNasalPipe(normalizeTimeValue(interventionTime.getNasalPipe()));
        interventionTime.setFaceMask(normalizeTimeValue(interventionTime.getFaceMask()));
        interventionTime.setEndotrachealTube(normalizeTimeValue(interventionTime.getEndotrachealTube()));
        interventionTime.setVentilator(normalizeTimeValue(interventionTime.getVentilator()));
        interventionTime.setCprStartTime(normalizeTimeValue(interventionTime.getCprStartTime()));
        interventionTime.setCprEndTime(normalizeTimeValue(interventionTime.getCprEndTime()));
        interventionTime.setUltrasound(normalizeTimeValue(interventionTime.getUltrasound()));
        interventionTime.setCT(normalizeTimeValue(interventionTime.getCT()));
        interventionTime.setTourniquet(normalizeTimeValue(interventionTime.getTourniquet()));
        interventionTime.setBloodDraw(normalizeTimeValue(interventionTime.getBloodDraw()));
        interventionTime.setCatheter(normalizeTimeValue(interventionTime.getCatheter()));
        interventionTime.setGastricTube(normalizeTimeValue(interventionTime.getGastricTube()));
        interventionTime.setTransfusionStart(normalizeTimeValue(interventionTime.getTransfusionStart()));
        interventionTime.setTransfusionEnd(normalizeTimeValue(interventionTime.getTransfusionEnd()));
        interventionTime.setLeaveSurgeryTime(normalizeTimeValue(interventionTime.getLeaveSurgeryTime()));
        interventionTime.setDeathTime(normalizeTimeValue(interventionTime.getDeathTime()));
    }
    
    /**
     * 规范化时间值
     * 如果值为"否"、"无"、空字符串或null，则返回null
     * 否则返回原值（应该是正确的四位数字HHMM格式）
     */
    private String normalizeTimeValue(String timeValue) {
        if (timeValue == null) {
            return null;
        }
        
        // 去除前后空格
        String trimmed = timeValue.trim();
        
        // 如果为空字符串，返回null
        if (trimmed.isEmpty()) {
            return null;
        }
        
        // 如果是"否"或"无"，返回null
        if ("否".equals(trimmed) || "无".equals(trimmed)) {
            return null;
        }
        
        // 其他情况返回原值（应该是正确的四位数字HHMM格式）
        return trimmed;
    }
    
    /**
     * 对无日期的事件时间应用跨天偏移逻辑：如果事件时间早于入室时间，则存为 2400 + 事件时间
     */
    private void applyCrossDayOffsets(InterventionTime it) {
        String admission = it.getAdmissionTime();
        if (admission == null || admission.length() != 4) {
            return;
        }
        it.setPeripheral(offsetIfCrossDay(admission, it.getPeripheral()));
        it.setIvLine(offsetIfCrossDay(admission, it.getIvLine()));
        it.setCentralAccess(offsetIfCrossDay(admission, it.getCentralAccess()));
        it.setNasalPipe(offsetIfCrossDay(admission, it.getNasalPipe()));
        it.setFaceMask(offsetIfCrossDay(admission, it.getFaceMask()));
        it.setEndotrachealTube(offsetIfCrossDay(admission, it.getEndotrachealTube()));
        it.setVentilator(offsetIfCrossDay(admission, it.getVentilator()));
        it.setCprStartTime(offsetIfCrossDay(admission, it.getCprStartTime()));
        it.setCprEndTime(offsetIfCrossDay(admission, it.getCprEndTime()));
        it.setUltrasound(offsetIfCrossDay(admission, it.getUltrasound()));
        it.setCT(offsetIfCrossDay(admission, it.getCT()));
        it.setTourniquet(offsetIfCrossDay(admission, it.getTourniquet()));
        it.setBloodDraw(offsetIfCrossDay(admission, it.getBloodDraw()));
        it.setCatheter(offsetIfCrossDay(admission, it.getCatheter()));
        it.setGastricTube(offsetIfCrossDay(admission, it.getGastricTube()));
        it.setTransfusionStart(offsetIfCrossDay(admission, it.getTransfusionStart()));
        // 按需求未包含transfusion_end，不处理
    }
    
    private String offsetIfCrossDay(String admission, String event) {
        if (admission == null || event == null) return event;
        if (admission.length() != 4 || event.length() != 4) return event;
        try {
            int a = Integer.parseInt(admission);
            int e = Integer.parseInt(event);
            if (e < a) {
                return String.format("%04d", 2400 + e);
            }
        } catch (NumberFormatException ignored) {
        }
        return event;
    }
}
