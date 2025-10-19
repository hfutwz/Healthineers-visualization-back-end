package com.demo.Service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.InterventionTime;
import com.demo.dto.TimelineEventDTO;
import com.demo.dto.TimelineStatisticsDTO;
import java.util.List;

public interface IInterventionTimeService extends IService<InterventionTime> {
    List<InterventionTime> getByPatientId(Integer patientId);
    
    /**
     * 获取患者时间线事件
     */
    List<TimelineEventDTO> getTimelineEvents(Integer patientId);
    
    /**
     * 获取事件统计信息
     */
    TimelineStatisticsDTO getEventStatistics(String eventType);
    
    /**
     * 获取事件统计信息（支持指定当前患者ID）
     */
    TimelineStatisticsDTO getEventStatistics(String eventType, Integer currentPatientId);
    
    /**
     * 获取关键事件
     */
    List<TimelineEventDTO> getKeyEvents(Integer patientId);
    
    /**
     * 获取非关键事件
     */
    List<TimelineEventDTO> getNonKeyEvents(Integer patientId);
}

