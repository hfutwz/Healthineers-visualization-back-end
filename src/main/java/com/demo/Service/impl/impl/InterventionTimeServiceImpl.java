package com.demo.Service.impl.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.entity.InterventionTime;
import com.demo.mapper.InterventionTimeMapper;
import com.demo.Service.impl.IInterventionTimeService;
import com.demo.dto.TimelineEventDTO;
import com.demo.dto.TimelineStatisticsDTO;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
            int hour = Integer.parseInt(timeStr.substring(0, 2));
            int minute = Integer.parseInt(timeStr.substring(2, 4));
            
            // 验证时间有效性：过滤超过2400的异常时间
            if (hour > 23 || minute > 59) {
                return null; // 返回null表示无效时间，不展示
            }
            
            // 处理跨天情况：如果时间小于入室时间，则认为是第二天
            LocalTime time = LocalTime.of(hour, minute);
            
            return LocalDateTime.of(date, time);
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
}
